import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Persists immutable smoke PASS proofs and binds their aggregate to the current tree. */
final class SmokeReceiptCache {
    private final Path root, objects, reports, logs;
    private final SmokeInputFingerprint fingerprints;
    private final SmokePins pins;
    private final SmokeGitState state;
    private final boolean reuse;
    private final List<Result> results = new ArrayList<>();

    SmokeReceiptCache(Path root) throws Exception {
        this(root, cacheRoot(root), !"off".equalsIgnoreCase(
                System.getenv().getOrDefault("WORLDLINE_SMOKE_CACHE", "on")));
    }

    SmokeReceiptCache(Path root, Path cache, boolean reuse) throws Exception {
        this.root = root.toAbsolutePath().normalize(); this.fingerprints = new SmokeInputFingerprint(root);
        this.pins = new SmokePins(root);
        this.state = SmokeGitState.read(root); this.reuse = reuse;
        this.objects = cache.resolve("smoke-results");
        this.reports = root.resolve(".worldline/reports/smokes");
        this.logs = root.resolve(".worldline/smoke-logs");
    }

    String fingerprint(SmokeDiscovery.Entry smoke) throws Exception { return fingerprints.compute(smoke); }

    boolean restore(SmokeDiscovery.Entry smoke, String fingerprint) throws Exception {
        if (!reuse) return false;
        Path proof = proof(smoke.id, fingerprint), evidence = evidence(smoke.id, fingerprint);
        if (validProof(smoke.id, fingerprint, proof, evidence)) {
            CacheUsage.touch(proof);
            Files.createDirectories(logs); Files.copy(evidence, logs.resolve(smoke.id + ".log"),
                    StandardCopyOption.REPLACE_EXISTING);
            record(smoke.id, fingerprint, digest(proof), "reused"); return true;
        }
        SmokePins.Entry pin = pins.verifiedMatch(smoke.id, fingerprint); if (pin == null) return false;
        record(smoke.id, fingerprint, pins.proof(pin), "pinned"); return true;
    }

    void passed(SmokeDiscovery.Entry smoke, String fingerprint, long duration) throws Exception {
        Path log = logs.resolve(smoke.id + ".log"); require(Files.isRegularFile(log),
                "missing PASS evidence for " + smoke.id);
        Path proof = proof(smoke.id, fingerprint), evidence = evidence(smoke.id, fingerprint);
        if (!validProof(smoke.id, fingerprint, proof, evidence)) {
            Files.createDirectories(proof.getParent()); atomicCopy(log, evidence);
            Properties values = new Properties();
            values.setProperty("schema", "1"); values.setProperty("status", "passed");
            values.setProperty("id", smoke.id); values.setProperty("fingerprint", fingerprint);
            values.setProperty("executed.at", Instant.now().toString());
            values.setProperty("executed.head", state.head());
            values.setProperty("executed.clean", Boolean.toString(state.clean()));
            values.setProperty("duration.ms", Long.toString(duration));
            values.setProperty("evidence.sha256", digest(evidence));
            atomicStore(proof, values, "Worldline immutable smoke PASS proof");
        }
        CacheUsage.touch(proof);
        record(smoke.id, fingerprint, digest(proof), "executed");
    }

    private static boolean validProof(String id, String fingerprint, Path proof, Path evidence)
            throws Exception {
        if (!Files.isRegularFile(proof) || !Files.isRegularFile(evidence)) return false;
        Properties values = load(proof);
        return "1".equals(values.getProperty("schema"))
                && "passed".equals(values.getProperty("status"))
                && id.equals(values.getProperty("id"))
                && fingerprint.equals(values.getProperty("fingerprint"))
                && digest(evidence).equals(values.getProperty("evidence.sha256"));
    }

    void finish(int expected) throws Exception {
        require(results.size() == expected, "smoke suite has incomplete receipts");
        MessageDigest aggregate = MessageDigest.getInstance("SHA-256");
        int executed = 0, pinned = 0;
        for (Result result : results) {
            update(aggregate, result.id); update(aggregate, result.fingerprint); update(aggregate, result.proof);
            if ("executed".equals(result.mode)) executed++;
            if ("pinned".equals(result.mode)) pinned++;
        }
        String rootHash = HexFormat.of().formatHex(aggregate.digest());
        String json = "{\n  \"schema\": 1,\n  \"status\": \"passed\",\n  \"qualified_at\": \""
                + Instant.now() + "\",\n  \"head\": \"" + state.head() + "\",\n  \"tree\": \""
                + state.tree() + "\",\n  \"clean\": " + state.clean() + ",\n  \"count\": " + expected
                + ",\n  \"executed\": " + executed + ",\n  \"reused\": " + (expected - executed - pinned)
                + ",\n  \"pinned\": " + pinned
                + ",\n  \"root_sha256\": \"" + rootHash + "\"\n}\n";
        atomicWrite(root.resolve(".worldline/reports/smoke-suite.json"), json);
        System.out.println("  smoke cache: " + (expected - executed - pinned) + " local, "
                + pinned + " pinned, " + executed + " executed");
        System.out.println("  smoke suite receipt: .worldline/reports/smoke-suite.json");
    }

    static String validateSuite(Path root, String head, String tree) throws Exception {
        Path suite = root.resolve(".worldline/reports/smoke-suite.json");
        require(Files.isRegularFile(suite), "run Gate.java --smoke before orchestrator qualification");
        String json = Files.readString(suite, StandardCharsets.UTF_8);
        java.util.Map<String, Object> document = MiniJson.object(json);
        require("passed".equals(MiniJson.string(document, "status")), "smoke suite receipt did not pass");
        require(head.equals(MiniJson.string(document, "head"))
                        && tree.equals(MiniJson.string(document, "tree")),
                "smoke suite receipt belongs to another commit");
        require(MiniJson.bool(document, "clean"), "smoke suite receipt was produced from a dirty worktree");
        SmokeReceiptCache cache = new SmokeReceiptCache(root, cacheRoot(root), true);
        List<SmokeDiscovery.Entry> smokes = SmokeDiscovery.discover(root);
        cache.pins.validateCatalog(smokes);
        require(smokes.size() == MiniJson.integer(document, "count"),
                "smoke suite receipt count drifted");
        MessageDigest aggregate = MessageDigest.getInstance("SHA-256");
        for (SmokeDiscovery.Entry smoke : smokes) cache.validate(smoke, head, tree, aggregate);
        require(HexFormat.of().formatHex(aggregate.digest()).equals(
                MiniJson.string(document, "root_sha256")),
                "smoke suite aggregate hash drifted");
        return digest(suite);
    }

    private void validate(SmokeDiscovery.Entry smoke, String head, String tree,
            MessageDigest aggregate) throws Exception {
        Properties attestation = load(reports.resolve(smoke.id + ".properties"));
        String fingerprint = fingerprints.compute(smoke);
        require("passed".equals(attestation.getProperty("status"))
                && smoke.id.equals(attestation.getProperty("id"))
                && fingerprint.equals(attestation.getProperty("fingerprint"))
                && head.equals(attestation.getProperty("head"))
                && tree.equals(attestation.getProperty("tree"))
                && "true".equals(attestation.getProperty("clean")),
                "invalid current-tree smoke attestation: " + smoke.id);
        String proofDigest = proofDigest(smoke, fingerprint, attestation.getProperty("mode"));
        require(proofDigest.equals(attestation.getProperty("proof.sha256")),
                "smoke proof attestation drifted: " + smoke.id);
        update(aggregate, smoke.id); update(aggregate, fingerprint); update(aggregate, proofDigest);
    }

    SmokePins.Entry availablePin(SmokeDiscovery.Entry smoke) throws Exception {
        String fingerprint = fingerprints.compute(smoke);
        Path proof = proof(smoke.id, fingerprint), evidence = evidence(smoke.id, fingerprint);
        if (validProof(smoke.id, fingerprint, proof, evidence)) return new SmokePins.Entry(
                smoke.id, fingerprint, load(proof).getProperty("evidence.sha256"), "executed");
        return pins.verifiedMatch(smoke.id, fingerprint);
    }

    long historicalDuration(String id) throws Exception {
        Path directory = objects.resolve(id); if (!Files.isDirectory(directory)) return Long.MAX_VALUE;
        long best = Long.MAX_VALUE;
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".properties")).toList()) {
                String value = load(path).getProperty("duration.ms");
                if (value == null) continue;
                try { best = Math.min(best, Long.parseLong(value)); }
                catch (NumberFormatException ignored) { }
            }
        }
        return best;
    }

    private String proofDigest(SmokeDiscovery.Entry smoke, String fingerprint, String mode)
            throws Exception {
        if ("pinned".equals(mode)) {
            SmokePins.Entry pin = pins.verifiedMatch(smoke.id, fingerprint);
            require(pin != null, "missing verified tracked smoke pin: " + smoke.id);
            return pins.proof(pin);
        }
        Path proof = proof(smoke.id, fingerprint), evidence = evidence(smoke.id, fingerprint);
        require(validProof(smoke.id, fingerprint, proof, evidence),
                "cached smoke proof drifted: " + smoke.id);
        return digest(proof);
    }

    private void record(String id, String fingerprint, String proof, String mode) throws Exception {
        Properties attestation = new Properties();
        attestation.setProperty("schema", "1"); attestation.setProperty("status", "passed");
        attestation.setProperty("id", id); attestation.setProperty("fingerprint", fingerprint);
        attestation.setProperty("proof.sha256", proof); attestation.setProperty("mode", mode);
        attestation.setProperty("head", state.head()); attestation.setProperty("tree", state.tree());
        attestation.setProperty("clean", Boolean.toString(state.clean()));
        atomicStore(reports.resolve(id + ".properties"), attestation,
                "Worldline current-tree smoke attestation");
        results.add(new Result(id, fingerprint, proof, mode));
    }

    private Path proof(String id, String fingerprint) {
        return objects.resolve(id).resolve(fingerprint + ".properties");
    }
    private Path evidence(String id, String fingerprint) {
        return objects.resolve(id).resolve(fingerprint + ".log");
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values; }
    private static void atomicStore(Path path, Properties values, String comment) throws Exception {
        Files.createDirectories(path.getParent()); Path temporary = temporary(path);
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            values.store(writer, comment);
        }
        move(temporary, path);
    }
    private static void atomicWrite(Path path, String value) throws Exception {
        Files.createDirectories(path.getParent()); Path temporary = temporary(path);
        Files.writeString(temporary, value, StandardCharsets.UTF_8); move(temporary, path);
    }
    private static void atomicCopy(Path source, Path target) throws Exception {
        Files.createDirectories(target.getParent()); Path temporary = temporary(target);
        Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING); move(temporary, target);
    }
    private static Path temporary(Path path) { return path.resolveSibling(path.getFileName() + ".tmp-"
            + ProcessHandle.current().pid() + "-" + System.nanoTime()); }
    private static void move(Path source, Path target) throws Exception { try {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException error) {
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    } }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    static Path cacheRoot(Path root) {
        String control = System.getenv("WORLDLINE_GATE_CONTROL");
        if (control != null && !control.isBlank()) return Path.of(control).resolve("cache");
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        String base = os.contains("win") ? System.getenv("LOCALAPPDATA") : System.getenv("XDG_RUNTIME_DIR");
        if (base == null || base.isBlank()) base = System.getProperty("java.io.tmpdir");
        return Path.of(base).toAbsolutePath().normalize().resolve("worldline/locks/cache");
    }
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message); }
    private record Result(String id, String fingerprint, String proof, String mode) {}
}
