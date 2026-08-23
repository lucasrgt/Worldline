import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/** Reuses the nested Gate lock self-test only for an exact harness and machine fingerprint. */
final class HarnessSelfTestCache {
    private final Path root;
    private final Path objects;

    HarnessSelfTestCache(Path root) {
        this.root = root;
        String control = System.getenv("WORLDLINE_GATE_CONTROL");
        if (control == null || control.isBlank())
            throw new IllegalStateException("missing gate control directory");
        objects = Path.of(control).resolve("cache/harness-self-tests");
    }

    void execute() throws Exception {
        String fingerprint = fingerprint();
        Path proof = objects.resolve(fingerprint + ".properties");
        Path evidence = objects.resolve(fingerprint + ".log");
        boolean reuse = !"off".equalsIgnoreCase(
                System.getenv().getOrDefault("WORLDLINE_SELF_TEST_CACHE", "on"));
        if (reuse && valid(proof, evidence, fingerprint)) {
            CacheUsage.touch(proof);
            System.out.println("Gate self-test restored for harness " + fingerprint.substring(0, 12));
            return;
        }
        String output = ProcessCapture.require(root, List.of(javaTool("java"), "-cp",
                required("WORLDLINE_HARNESS_CP"), "Gate", "--self-test"), 30);
        if (!output.isBlank()) System.out.print(output);
        Files.createDirectories(objects);
        atomicWrite(evidence, output);
        Properties values = new Properties();
        values.setProperty("schema", "1");
        values.setProperty("status", "passed");
        values.setProperty("fingerprint", fingerprint);
        values.setProperty("executed.at", Instant.now().toString());
        values.setProperty("evidence.sha256", digest(evidence));
        atomicStore(proof, values);
        CacheUsage.touch(proof);
    }

    private String fingerprint() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, "worldline-harness-self-test-v1");
        update(digest, System.getProperty("java.version"));
        update(digest, System.getProperty("os.name"));
        update(digest, System.getProperty("os.version"));
        Path control = objects.getParent().getParent();
        update(digest, Files.getFileStore(control).name());
        update(digest, Files.getFileStore(control).type());
        try (Stream<Path> paths = Files.list(root.resolve("tools/harness"))) {
            for (Path source : paths.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
                update(digest, root.relativize(source).toString().replace('\\', '/'));
                digest.update(Files.readAllBytes(source));
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static boolean valid(Path proof, Path evidence, String fingerprint) throws Exception {
        if (!Files.isRegularFile(proof) || !Files.isRegularFile(evidence)) return false;
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(proof, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return "1".equals(values.getProperty("schema"))
                && "passed".equals(values.getProperty("status"))
                && fingerprint.equals(values.getProperty("fingerprint"))
                && digest(evidence).equals(values.getProperty("evidence.sha256"));
    }

    private static void atomicStore(Path path, Properties values) throws Exception {
        Path temporary = temporary(path);
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            values.store(writer, "Worldline harness self-test PASS proof");
        }
        move(temporary, path);
    }

    private static void atomicWrite(Path path, String value) throws Exception {
        Path temporary = temporary(path);
        Files.writeString(temporary, value, StandardCharsets.UTF_8);
        move(temporary, path);
    }

    private static Path temporary(Path path) throws Exception {
        Files.createDirectories(path.getParent());
        return path.resolveSibling(path.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
    }

    private static void move(Path source, Path target) throws Exception {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String digest(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(Files.readAllBytes(path));
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing " + name);
        return value;
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
                name + (windows ? ".exe" : "")).toString();
    }
}
