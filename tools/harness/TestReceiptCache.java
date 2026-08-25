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
import java.util.Properties;

/** Stores immutable, content-addressed PASS evidence for unit-suite executions. */
final class TestReceiptCache {
    private static final String SCHEMA = "1";
    private final Path root;
    private final Path objects;
    private final boolean reuse;
    private int restored;
    private int executed;

    TestReceiptCache(Path root, Path objects) {
        this.root = root;
        this.objects = objects;
        reuse = !"off".equalsIgnoreCase(System.getenv().getOrDefault("WORLDLINE_TEST_CACHE", "on"));
    }

    String fingerprint(String suite, String testDigest) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, "worldline-test-receipt-v2");
        update(digest, suite);
        update(digest, testDigest);
        update(digest, System.getProperty("java.version"));
        update(digest, System.getProperty("os.name"));
        update(digest, System.getProperty("os.arch"));
        update(digest, "assertions=enabled");
        digest.update(Files.readAllBytes(root.resolve("tools/harness/TestBuild.java")));
        digest.update(Files.readAllBytes(root.resolve("tools/harness/TestReceiptCache.java")));
        return HexFormat.of().formatHex(digest.digest());
    }

    boolean restore(String suite, String fingerprint) throws Exception {
        if (!reuse) return false;
        if (sampledForRecheck(suite, fingerprint)) return false;
        Path proof = proof(suite, fingerprint);
        Path evidence = evidence(suite, fingerprint);
        if (!Files.isRegularFile(proof) || !Files.isRegularFile(evidence)) return false;
        Properties values = load(proof);
        boolean valid = SCHEMA.equals(values.getProperty("schema"))
                && "passed".equals(values.getProperty("status"))
                && suite.equals(values.getProperty("suite"))
                && fingerprint.equals(values.getProperty("fingerprint"))
                && digest(evidence).equals(values.getProperty("evidence.sha256"));
        if (valid) { restored++; CacheUsage.touch(proof); }
        return valid;
    }

    private static boolean sampledForRecheck(String suite, String fingerprint) throws Exception {
        int percent = environmentInteger("WORLDLINE_TEST_CACHE_RECHECK_PERCENT", 0, 0, 100);
        if (percent == 0) return false;
        String seed = System.getenv().getOrDefault("WORLDLINE_TEST_CACHE_RECHECK_SEED",
                java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString());
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, seed); update(digest, suite); update(digest, fingerprint);
        return Byte.toUnsignedInt(digest.digest()[0]) * 100 / 256 < percent;
    }

    static boolean sampledForRecheck(String seed, String suite, String fingerprint,
            int percent) throws Exception {
        require(percent >= 0 && percent <= 100, "invalid recheck percent");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, seed); update(digest, suite); update(digest, fingerprint);
        return Byte.toUnsignedInt(digest.digest()[0]) * 100 / 256 < percent;
    }

    void passed(String suite, String fingerprint, String output) throws Exception {
        Path proof = proof(suite, fingerprint);
        Path evidence = evidence(suite, fingerprint);
        Files.createDirectories(proof.getParent());
        atomicWrite(evidence, output);
        Properties values = new Properties();
        values.setProperty("schema", SCHEMA);
        values.setProperty("status", "passed");
        values.setProperty("suite", suite);
        values.setProperty("fingerprint", fingerprint);
        values.setProperty("executed.at", Instant.now().toString());
        values.setProperty("evidence.sha256", digest(evidence));
        atomicStore(proof, values);
        CacheUsage.touch(proof);
        executed++;
        GateWorkMetrics.testSuiteExecuted();
    }

    void failed(String suite, String fingerprint, int exit, String output) throws Exception {
        Path proof = proof(suite, fingerprint), evidence = evidence(suite, fingerprint);
        boolean removedPass = Files.isRegularFile(proof);
        Files.deleteIfExists(proof); Files.deleteIfExists(evidence);
        Path quarantine = quarantine(suite, fingerprint);
        Path log = quarantine.resolveSibling(fingerprint + ".log");
        atomicWrite(log, output);
        Properties values = new Properties();
        values.setProperty("schema", SCHEMA); values.setProperty("status", "failed");
        values.setProperty("suite", suite); values.setProperty("fingerprint", fingerprint);
        values.setProperty("exit", Integer.toString(exit));
        values.setProperty("detected.at", Instant.now().toString());
        values.setProperty("prior.pass.removed", Boolean.toString(removedPass));
        values.setProperty("evidence.sha256", digest(log));
        atomicStore(quarantine, values);
    }

    void finish(int expected) {
        require(restored + executed == expected, "unit-suite receipt count drifted");
        System.out.println("  test suite cache: " + restored + " restored, " + executed + " executed");
    }

    private Path proof(String suite, String fingerprint) {
        return objects.resolve(suite).resolve(fingerprint + ".properties");
    }

    private Path evidence(String suite, String fingerprint) {
        return objects.resolve(suite).resolve(fingerprint + ".log");
    }

    Path quarantine(String suite, String fingerprint) {
        return objects.resolve("quarantine").resolve(suite).resolve(fingerprint + ".properties");
    }

    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private static void atomicStore(Path path, Properties values) throws Exception {
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            values.store(writer, "Worldline immutable unit-suite PASS proof");
        }
        move(temporary, path);
    }

    private static void atomicWrite(Path path, String value) throws Exception {
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        Files.writeString(temporary, value, StandardCharsets.UTF_8);
        move(temporary, path);
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

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private static int environmentInteger(String name, int fallback, int minimum, int maximum) {
        String raw = System.getenv(name); int value = fallback;
        if (raw != null && !raw.isBlank()) try { value = Integer.parseInt(raw); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer"); }
        require(value >= minimum && value <= maximum,
                name + " must be between " + minimum + " and " + maximum);
        return value;
    }
}
