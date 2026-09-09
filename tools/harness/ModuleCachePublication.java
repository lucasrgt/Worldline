import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Prepares a closed module publication plan before any remote CAS is started. */
final class ModuleCachePublication {
    private static final String PROTOCOL = "{\"schema\":\"worldline.cas-transport-protocol.v1\","
            + "\"permits_schema\":\"worldline.cas-transport-permits.v1\","
            + "\"transport\":\"http-loopback-v1\"}\n";
    private static final String MAXIMUM = "8388608";

    private ModuleCachePublication() { }

    static Prepared prepare(Path repository, Path staging, List<Module> input,
            List<ArtifactTransportPolicy.Candidate> extras) throws Exception {
        Path root = repository.toAbsolutePath().normalize();
        Path output = staging.toAbsolutePath().normalize();
        require(output.startsWith(root.resolve(".worldline/cas-shadow")),
                "module publication staging escaped the CAS shadow root");
        List<Module> modules = sorted(input);
        require(!modules.isEmpty(), "module publication plan is empty");
        Files.createDirectories(output);
        ArtifactTransportPolicy policy = ArtifactTransportPolicy.load(root);
        List<PublishedModule> published = new ArrayList<>();
        List<ArtifactTransportPolicy.Candidate> candidates = new ArrayList<>(extras);
        List<ModuleCacheIndex.Entry> entries = new ArrayList<>();
        for (Module module : modules) {
            Path bundle = output.resolve(module.id() + ".wmb");
            ModuleCacheBundle.Identity identity = ModuleCacheBundle.pack(
                    module.id(), module.entry(), bundle);
            ArtifactTransportPolicy.Candidate candidate = policy.authorizeGenerated(
                    "shared-module-cache", bundle);
            published.add(new PublishedModule(identity, candidate)); candidates.add(candidate);
            entries.add(new ModuleCacheIndex.Entry(identity.module(), identity.input(),
                    identity.javaVersion(), candidate.sha256(), candidate.size()));
        }
        Path indexPath = output.resolve("modules.wmi");
        ModuleCacheIndex.write(indexPath, entries);
        ArtifactTransportPolicy.Candidate index = policy.authorizeGenerated(
                "module-cache-index", indexPath);
        candidates.add(index);
        Path permits = output.resolve("permits.json");
        Files.writeString(permits, ArtifactTransportPermitQuery.manifest(candidates),
                StandardCharsets.UTF_8);
        return new Prepared(List.copyOf(published), index, List.copyOf(candidates), permits);
    }

    static void upload(Path root, Path binary, String address, Path local, Prepared prepared)
            throws Exception {
        require(PROTOCOL.equals(ProcessCapture.require(root,
                List.of(binary.toString(), "protocol"), 30)),
                "unsupported CAS transport protocol");
        require(loopback(address), "module publication CAS is not loopback");
        String expectedPermits = ArtifactTransportPermitQuery.manifest(prepared.candidates());
        require(Files.readString(prepared.permits(), StandardCharsets.UTF_8)
                .equals(expectedPermits), "module publication permit manifest drifted");
        for (PublishedModule module : prepared.modules())
            upload(root, binary, local, address, prepared.permits(), module.candidate());
        upload(root, binary, local, address, prepared.permits(), prepared.index());
    }

    static void selfTest() throws Exception {
        Path source = Path.of("").toAbsolutePath().normalize();
        Path root = Files.createTempDirectory("worldline-module-publication-");
        try {
            Files.createDirectories(root.resolve("quality"));
            Files.copy(source.resolve("quality/artifact-transport.properties"),
                    root.resolve("quality/artifact-transport.properties"),
                    StandardCopyOption.COPY_ATTRIBUTES);
            Path entry = root.resolve("entry"); Files.createDirectories(entry.resolve("fixture"));
            Files.writeString(entry.resolve(".complete"), "a".repeat(64) + "\n");
            Files.write(entry.resolve("fixture/Example.class"),
                    new byte[] {-54, -2, -70, -66, 0, 1});
            Module module = new Module("fixture", entry);
            Prepared plan = prepare(root, root.resolve(".worldline/cas-shadow/first"),
                    List.of(module), List.of());
            PublishedModule published = plan.modules().get(0);
            ModuleCacheIndex.Entry lookup = ModuleCacheIndex.read(plan.index().path()).lookup(
                    published.identity().module(), published.identity().input(),
                    published.identity().javaVersion());
            require(lookup != null && lookup.blobSha256().equals(
                    published.candidate().sha256()), "prepared module index drifted");
            require(Files.readString(plan.permits()).equals(
                    ArtifactTransportPermitQuery.manifest(plan.candidates())),
                    "prepared module permits drifted");
            rejected(() -> prepare(root, root.resolve(".worldline/cas-shadow/duplicate"),
                    List.of(module, module), List.of()), "duplicate module was planned");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  module cache publication self-test: passed");
    }

    private static List<Module> sorted(List<Module> input) {
        List<Module> modules = new ArrayList<>(input); Set<String> ids = new HashSet<>();
        modules.sort(Comparator.comparing(Module::id));
        for (Module module : modules) {
            require(module.id().matches("[a-z0-9]+(?:-[a-z0-9]+)*"),
                    "invalid module publication name");
            require(ids.add(module.id()), "duplicate module publication: " + module.id());
        }
        return List.copyOf(modules);
    }

    private static void upload(Path root, Path binary, Path local, String address, Path permits,
            ArtifactTransportPolicy.Candidate candidate) throws Exception {
        String expected = candidate.sha256() + " " + candidate.size() + "\n";
        require(expected.equals(command(root, binary, "put", local, candidate.path())),
                "local CAS publication drifted");
        require(expected.equals(command(root, binary, "upload", local, address, permits,
                candidate.sha256(), MAXIMUM)), "CAS upload identity drifted");
    }

    private static boolean loopback(String address) {
        if (!address.matches("127\\.0\\.0\\.1:[1-9][0-9]{0,4}")) return false;
        return Integer.parseInt(address.substring(address.lastIndexOf(':') + 1)) <= 65_535;
    }
    private static String command(Path root, Path binary, Object... arguments) throws Exception {
        List<String> command = new ArrayList<>(); command.add(binary.toString());
        for (Object argument : arguments) command.add(argument.toString());
        return ProcessCapture.require(root, command, 60);
    }
    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false;
        try { action.run(); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Module(String id, Path entry) { }
    record PublishedModule(ModuleCacheBundle.Identity identity,
            ArtifactTransportPolicy.Candidate candidate) { }
    record Prepared(List<PublishedModule> modules, ArtifactTransportPolicy.Candidate index,
            List<ArtifactTransportPolicy.Candidate> candidates, Path permits) { }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
