import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

/** Content-addressed compilation and self-test gate for every Runtime Fabric backend. */
final class RuntimeFabricCheck {
    private final Path root;
    RuntimeFabricCheck(Path root) { this.root = root; }

    void execute() throws Exception {
        String digest = digest(); Path proof = cache().resolve(digest + ".properties");
        if (valid(proof, digest)) {
            System.out.println("  Runtime Fabric self-test restored for " + digest.substring(0, 12));
            return;
        }
        Path build = Files.createTempDirectory(root.resolve(".worldline"), "runtime-fabric-check-");
        try {
            compile(build);
            for (String type : List.of("ContainerSmokePool", "HostSmokePool", "RuntimeFabric"))
                ProcessCapture.require(root, List.of(java(), "-cp", build.toString(), type, "--self-test"), 120);
            store(proof, digest);
            System.out.println("  Runtime Fabric: container, host and backend admission self-tests passed");
        } finally { SafeTreeDelete.delete(build); }
    }

    private void compile(Path output) throws Exception {
        List<String> command = new ArrayList<>(List.of(javac(), "-encoding", "UTF-8", "--release", "21",
                "-Xlint:all,-options", "-Werror", "-d", output.toString()));
        try (Stream<Path> paths = Files.list(root.resolve("tools/containers"))) {
            command.addAll(paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted().map(Path::toString).toList());
        }
        ProcessCapture.require(root, command, 180);
    }

    private String digest() throws Exception {
        MessageDigest value = MessageDigest.getInstance("SHA-256");
        update(value, "worldline-runtime-fabric-check-v1"); update(value, System.getProperty("java.version"));
        update(value, System.getProperty("os.name"));
        try (Stream<Path> paths = Files.walk(root.resolve("tools/containers"))) {
            for (Path path : paths.filter(Files::isRegularFile).sorted(Comparator.naturalOrder()).toList()) {
                update(value, root.relativize(path).toString().replace('\\', '/'));
                value.update(Files.readAllBytes(path));
            }
        }
        for (Path path : List.of(root.resolve("tools/harness/PooledSmokeCheck.java"),
                root.resolve("tools/harness/SmokeGitState.java"),
                root.resolve("tools/harness/SmokeTrackedFiles.java"))) {
            update(value, root.relativize(path).toString().replace('\\', '/'));
            value.update(Files.readAllBytes(path));
        }
        return HexFormat.of().formatHex(value.digest());
    }

    private boolean valid(Path proof, String digest) {
        if (!Files.isRegularFile(proof)) return false;
        try (Reader reader = Files.newBufferedReader(proof, StandardCharsets.UTF_8)) {
            Properties values = new Properties(); values.load(reader);
            return "passed".equals(values.getProperty("status"))
                    && digest.equals(values.getProperty("digest"));
        } catch (Exception ignored) { return false; }
    }

    private void store(Path proof, String digest) throws Exception {
        Files.createDirectories(proof.getParent()); Properties values = new Properties();
        values.setProperty("status", "passed"); values.setProperty("digest", digest);
        Path temporary = Files.createTempFile(proof.getParent(), "runtime-fabric-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                values.store(writer, "Worldline Runtime Fabric self-test PASS proof");
            }
            try { Files.move(temporary, proof, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, proof, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }

    private Path cache() {
        String configured = System.getenv("WORLDLINE_GATE_CONTROL");
        if (configured != null && !configured.isBlank())
            return Path.of(configured).toAbsolutePath().normalize().resolve("cache/runtime-fabric");
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        String base = windows ? System.getenv("LOCALAPPDATA") : System.getenv("XDG_RUNTIME_DIR");
        if (base == null || base.isBlank()) base = System.getProperty("java.io.tmpdir");
        return Path.of(base).toAbsolutePath().normalize().resolve("worldline/locks/cache/runtime-fabric");
    }
    private static String java() { return tool("java"); }
    private static String javac() { return tool("javac"); }
    private static String tool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }
}
