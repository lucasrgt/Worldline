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

/** Preserves runtime observations independently from qualification policy. */
final class SmokeObservationCache {
    private final Path objects, logs;
    private final SmokeInputFingerprint fingerprints;

    SmokeObservationCache(Path root) throws Exception {
        this(root, SmokeReceiptCache.cacheRoot(root));
    }

    SmokeObservationCache(Path root, Path cache) throws Exception {
        this.fingerprints = new SmokeInputFingerprint(root);
        this.objects = cache.resolve("smoke-observations");
        this.logs = root.resolve(".worldline/smoke-logs");
    }

    String fingerprint(SmokeDiscovery.Entry smoke) throws Exception {
        return fingerprints.computeRuntime(smoke);
    }

    Observation restore(SmokeDiscovery.Entry smoke, String fingerprint) throws Exception {
        Path proof = proof(smoke.id, fingerprint), evidence = evidence(smoke.id, fingerprint);
        if (!valid(smoke.id, fingerprint, proof, evidence)) return null;
        Files.createDirectories(logs);
        Files.copy(evidence, logs.resolve(smoke.id + ".log"), StandardCopyOption.REPLACE_EXISTING);
        return new Observation(Long.parseLong(load(proof).getProperty("duration.ms")));
    }

    void observed(SmokeDiscovery.Entry smoke, String fingerprint, long duration) throws Exception {
        require(duration >= 0L, "invalid runtime observation duration");
        Path log = logs.resolve(smoke.id + ".log");
        require(Files.isRegularFile(log), "missing runtime observation for " + smoke.id);
        Path proof = proof(smoke.id, fingerprint), evidence = evidence(smoke.id, fingerprint);
        if (valid(smoke.id, fingerprint, proof, evidence)) return;
        Files.createDirectories(proof.getParent()); atomicCopy(log, evidence);
        Properties values = new Properties();
        values.setProperty("schema", "1"); values.setProperty("status", "observed");
        values.setProperty("id", smoke.id); values.setProperty("fingerprint", fingerprint);
        values.setProperty("observed.at", Instant.now().toString());
        values.setProperty("duration.ms", Long.toString(duration));
        values.setProperty("evidence.sha256", digest(evidence));
        atomicStore(proof, values, "Worldline immutable runtime observation");
    }

    private static boolean valid(String id, String fingerprint, Path proof, Path evidence)
            throws Exception {
        if (!Files.isRegularFile(proof) || !Files.isRegularFile(evidence)) return false;
        Properties values = load(proof);
        String duration = values.getProperty("duration.ms", "");
        return "1".equals(values.getProperty("schema"))
                && "observed".equals(values.getProperty("status"))
                && id.equals(values.getProperty("id"))
                && fingerprint.equals(values.getProperty("fingerprint"))
                && duration.matches("[0-9]+")
                && digest(evidence).equals(values.getProperty("evidence.sha256"));
    }

    private Path proof(String id, String fingerprint) {
        return objects.resolve(id).resolve(fingerprint + ".properties");
    }

    private Path evidence(String id, String fingerprint) {
        return objects.resolve(id).resolve(fingerprint + ".log");
    }

    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private static void atomicStore(Path path, Properties values, String comment) throws Exception {
        Files.createDirectories(path.getParent()); Path temporary = temporary(path);
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            values.store(writer, comment);
        }
        move(temporary, path);
    }

    private static void atomicCopy(Path source, Path target) throws Exception {
        Files.createDirectories(target.getParent()); Path temporary = temporary(target);
        Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING); move(temporary, target);
    }

    private static Path temporary(Path path) {
        return path.resolveSibling(path.getFileName() + ".tmp-" + ProcessHandle.current().pid()
                + "-" + System.nanoTime());
    }

    private static void move(Path source, Path target) throws Exception {
        try { Files.move(source, target, StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException error) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    record Observation(long duration) { }
}
