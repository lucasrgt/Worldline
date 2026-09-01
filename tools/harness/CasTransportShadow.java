import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Exercises policy-authorized Rust CAS transport without mutating canonical caches. */
public final class CasTransportShadow {
    private static final String PROTOCOL = "{\"schema\":\"worldline.cas-transport-protocol.v1\","
            + "\"permits_schema\":\"worldline.cas-transport-permits.v1\","
            + "\"transport\":\"http-loopback-v1\"}\n";
    private static final String MAXIMUM = "8388608";

    private CasTransportShadow() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: Gate.java --cas-shadow");
            execute(Path.of("").toAbsolutePath().normalize(), binary());
        } catch (Exception error) {
            System.err.println("CAS transport shadow failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void selfTest() {
        require(parseListen("worldline.cas-listen.v1 127.0.0.1:49152\n").equals("127.0.0.1:49152"),
                "CAS listen handshake drifted");
        rejected(() -> parseListen("worldline.cas-listen.v1 0.0.0.0:80\n"),
                "non-loopback CAS listener was accepted");
    }

    private static void execute(Path root, Path binary) throws Exception {
        require(PROTOCOL.equals(ProcessCapture.require(root, List.of(binary.toString(), "protocol"), 30)),
                "unsupported CAS transport protocol");
        ArtifactTransportPolicy policy = ArtifactTransportPolicy.load(root);
        List<ArtifactTransportPolicy.Candidate> tracked = policy.eligibleCandidates(
                SmokeTrackedFiles.read(root));
        require(!tracked.isEmpty(), "no tracked CAS transport candidates");
        Path base = root.resolve(".worldline/cas-shadow"); Files.createDirectories(base);
        Path run = Files.createTempDirectory(base, "run-");
        Process server = null;
        try {
            Path moduleEntry = moduleEntry(root, "api");
            ModuleCachePublication.Prepared prepared = ModuleCachePublication.prepare(root, run,
                    List.of(new ModuleCachePublication.Module("api", moduleEntry)), tracked);
            ModuleCachePublication.PublishedModule published = prepared.modules().get(0);
            ModuleCacheBundle.Identity identity = published.identity();
            ArtifactTransportPolicy.Candidate module = published.candidate();
            ArtifactTransportPolicy.Candidate index = prepared.index();
            Path permits = prepared.permits();
            Path local = run.resolve("local"), remote = run.resolve("remote");
            Path fresh = run.resolve("fresh");
            Path errors = run.resolve("server.err");
            server = start(root, binary, remote, permits, errors);
            String address = listen(server, errors);
            ArtifactTransportPolicy.Candidate proof = tracked.get(0);
            Path proofOutput = transport(root, binary, local, fresh, address, permits,
                    proof, run.resolve("hydrated.proof"));
            policy.validateHydrated(proof, proofOutput);
            require(Files.mismatch(proof.path(), proofOutput) == -1L,
                    "hydrated bytes differ from authorized source");
            ModuleCachePublication.upload(root, binary, address, local, prepared);
            rejectUnplanned(root, binary, local, address, permits, run.resolve("unplanned.bin"));
            Path indexOutput = hydrate(root, binary, fresh, address, permits,
                    index, run.resolve("hydrated.wmi"));
            policy.validateHydrated(index, indexOutput);
            ModuleCacheIndex.Entry lookup = ModuleCacheIndex.read(indexOutput).lookup(
                    identity.module(), identity.input(), identity.javaVersion());
            require(lookup != null && lookup.blobSha256().equals(module.sha256())
                    && lookup.blobSize() == module.size(), "module cache index lookup drifted");
            Path restored = run.resolve("restored-module");
            java.util.Map<String, String> environment = java.util.Map.of(
                    "WORLDLINE_MODULE_CAS_MODE", "shadow",
                    "WORLDLINE_CAS_BINARY", binary.toString(),
                    "WORLDLINE_MODULE_CAS_ADDRESS", address,
                    "WORLDLINE_MODULE_CAS_PERMITS", permits.toString(),
                    "WORLDLINE_MODULE_CAS_INDEX", indexOutput.toString(),
                    "WORLDLINE_MODULE_CAS_INDEX_SHA256", index.sha256());
            require(ModuleCacheRemote.hydrate(root, run.resolve("module-cache"),
                    identity.module(), identity.input(), restored, environment),
                    "module cache hydrator reported a miss");
            ModuleCacheBundle.requireEqual(moduleEntry, restored);
            System.out.println("CAS transport shadow passed; proof.sha256=" + proof.sha256()
                    + ";proof.size=" + proof.size() + ";module=api;module.input="
                    + identity.input() + ";module.sha256=" + module.sha256()
                    + ";module.size=" + module.size() + ";index.sha256=" + index.sha256()
                    + ";index.size=" + index.size());
        } finally {
            try {
                if (server != null && server.isAlive()) ProcessCapture.destroy(server);
            } finally { SafeTreeDelete.delete(run); }
        }
    }

    private static Path moduleEntry(Path root, String module) throws Exception {
        Path output = root.resolve(".worldline/build/classes").resolve(module);
        require(Files.isDirectory(output), "run canonical Gate before CAS module shadow");
        Path entry = output.toRealPath();
        require(Files.isRegularFile(entry.resolve(".complete")),
                "compiled module cache entry is incomplete: " + module);
        return entry;
    }

    private static Path transport(Path root, Path binary, Path local, Path fresh,
            String address, Path permits, ArtifactTransportPolicy.Candidate candidate,
            Path output) throws Exception {
        upload(root, binary, local, address, permits, candidate);
        return hydrate(root, binary, fresh, address, permits, candidate, output);
    }

    private static Path hydrate(Path root, Path binary, Path fresh, String address, Path permits,
            ArtifactTransportPolicy.Candidate candidate, Path output) throws Exception {
        String expected = candidate.sha256() + " " + candidate.size() + "\n";
        require(expected.equals(command(root, binary, "hydrate", fresh, address, permits,
                candidate.sha256(), MAXIMUM)), "CAS hydration identity drifted");
        command(root, binary, "get", fresh, candidate.sha256(), output); return output;
    }

    private static void rejectUnplanned(Path root, Path binary, Path local, String address,
            Path permits, Path source) throws Exception {
        Files.writeString(source, "not in the publication plan\n", StandardCharsets.UTF_8);
        String localIdentity = command(root, binary, "put", local, source);
        String digest = localIdentity.substring(0, localIdentity.indexOf(' '));
        ProcessCapture.Result result = ProcessCapture.run(root, List.of(binary.toString(), "upload",
                local.toString(), address, permits.toString(), digest, MAXIMUM), 60);
        require(!result.timedOut() && result.exit() != 0,
                "unplanned CAS object was remotely accepted");
    }

    private static void upload(Path root, Path binary, Path local, String address,
            Path permits, ArtifactTransportPolicy.Candidate candidate) throws Exception {
        String expected = candidate.sha256() + " " + candidate.size() + "\n";
        require(expected.equals(command(root, binary, "put", local, candidate.path())),
                "local CAS publication drifted");
        require(expected.equals(command(root, binary, "upload", local, address, permits,
                candidate.sha256(), MAXIMUM)), "CAS upload identity drifted");
    }

    private static Path binary() {
        String configured = System.getenv("WORLDLINE_CAS_BINARY");
        require(configured != null && !configured.isBlank(), "WORLDLINE_CAS_BINARY is required");
        Path path = Path.of(configured).toAbsolutePath().normalize();
        require(path.isAbsolute() && Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(path), "invalid WORLDLINE_CAS_BINARY");
        return path;
    }

    private static Process start(Path root, Path binary, Path remote, Path permits, Path errors)
            throws Exception {
        Process process = new ProcessBuilder(binary.toString(), "serve", remote.toString(),
                "127.0.0.1:0", permits.toString(), MAXIMUM).directory(root.toFile())
                .redirectError(errors.toFile()).start();
        process.getOutputStream().close(); return process;
    }

    private static String listen(Process process, Path errors) throws Exception {
        BufferedReader output = new BufferedReader(new InputStreamReader(
                process.getInputStream(), StandardCharsets.UTF_8));
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (!output.ready() && process.isAlive() && System.nanoTime() < deadline) Thread.sleep(20L);
        if (!output.ready()) throw new IllegalStateException("CAS server did not report readiness: "
                + Files.readString(errors, StandardCharsets.UTF_8));
        String line = output.readLine();
        require(line != null, "CAS server readiness stream ended");
        return parseListen(line + "\n");
    }

    private static String parseListen(String line) {
        String prefix = "worldline.cas-listen.v1 ";
        require(line.endsWith("\n") && line.startsWith(prefix), "invalid CAS listen handshake");
        String address = line.substring(prefix.length(), line.length() - 1);
        require(address.matches("127\\.0\\.0\\.1:[1-9][0-9]{0,4}"), "CAS listener is not loopback");
        int port = Integer.parseInt(address.substring(address.lastIndexOf(':') + 1));
        require(port <= 65_535, "invalid CAS listener port"); return address;
    }

    private static String command(Path root, Path binary, Object... arguments) throws Exception {
        java.util.ArrayList<String> command = new java.util.ArrayList<>();
        command.add(binary.toString());
        for (Object argument : arguments) command.add(argument.toString());
        return ProcessCapture.require(root, command, 60);
    }
    private static void rejected(Runnable action, String message) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
