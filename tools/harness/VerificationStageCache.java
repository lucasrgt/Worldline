import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/** Restores stable verification stages from platform-bound content receipts. */
final class VerificationStageCache {
    private static int restoredStages, executedStages;
    private final Path root, objects;
    private final boolean countMetrics;
    private final Map<Path, String> inputDigests = new HashMap<>();

    VerificationStageCache(Path root) {
        this.root = root; this.countMetrics = true;
        String control = System.getenv("WORLDLINE_GATE_CONTROL");
        if (control == null || control.isBlank())
            throw new IllegalStateException("missing gate control directory");
        objects = Path.of(control).resolve("cache/verification-stages");
        restoredStages = 0; executedStages = 0;
    }

    void execute(String stage, List<Path> inputs, VerifyReport.Checked action) throws Exception {
        String fingerprint = fingerprint(stage, inputs);
        Path proof = objects.resolve(stage).resolve(fingerprint + ".properties");
        if (valid(proof, fingerprint)) {
            if (countMetrics) restoredStages++;
            System.out.println("  verification stage restored: " + stage);
            return;
        }
        action.run(); store(proof, stage, fingerprint); if (countMetrics) executedStages++;
    }

    static Metrics metrics() { return new Metrics(restoredStages, executedStages); }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-stage-cache-");
        try {
            Path implementation = root.resolve("tools/harness/VerificationStageCache.java");
            Files.createDirectories(implementation.getParent());
            Files.copy(Path.of("tools/harness/VerificationStageCache.java"), implementation);
            VerificationStageCache cache = new VerificationStageCache(root,
                    root.resolve("objects"), false);
            Path input = root.resolve("input.txt"); Files.writeString(input, "one\n");
            int[] executions = {0}; VerifyReport.Checked action = () -> executions[0]++;
            cache.execute("test", List.of(input), action); cache.execute("test", List.of(input), action);
            Path proof;
            try (Stream<Path> paths = Files.walk(root.resolve("objects"))) {
                proof = paths.filter(path -> path.toString().endsWith(".properties")).findFirst().orElseThrow();
            }
            Files.writeString(proof, "corrupt\n"); cache.execute("test", List.of(input), action);
            Files.writeString(input, "two\n");
            new VerificationStageCache(root, root.resolve("objects"), false)
                    .execute("test", List.of(input), action);
            if (executions[0] != 3) throw new IllegalStateException("verification stage cache invalidation drift");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  verification stage cache self-test: passed");
    }

    private VerificationStageCache(Path root, Path objects, boolean countMetrics) {
        this.root = root; this.objects = objects; this.countMetrics = countMetrics;
    }

    private String fingerprint(String stage, List<Path> inputs) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, "worldline-verification-stage-v1"); update(digest, stage);
        update(digest, System.getProperty("java.version"));
        update(digest, System.getProperty("os.name")); update(digest, System.getProperty("os.arch"));
        for (Path input : inputs.stream().map(path -> path.toAbsolutePath().normalize())
                .sorted().distinct().toList()) {
            update(digest, label(input)); update(digest, inputDigest(input));
        }
        Path implementation = root.resolve("tools/harness/VerificationStageCache.java");
        digest.update(Files.readAllBytes(implementation));
        return HexFormat.of().formatHex(digest.digest());
    }

    private String inputDigest(Path input) throws Exception {
        String cached = inputDigests.get(input); if (cached != null) return cached;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        if (!Files.exists(input)) update(digest, "missing");
        else if (Files.isRegularFile(input)) digest.update(Files.readAllBytes(input));
        else try (Stream<Path> paths = Files.walk(input)) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .sorted(Comparator.naturalOrder()).toList()) {
                update(digest, input.relativize(path).toString().replace('\\', '/'));
                digest.update(Files.readAllBytes(path));
            }
        }
        String value = HexFormat.of().formatHex(digest.digest()); inputDigests.put(input, value);
        return value;
    }

    private boolean valid(Path proof, String fingerprint) {
        if (!Files.isRegularFile(proof)) return false;
        try (Reader reader = Files.newBufferedReader(proof, StandardCharsets.UTF_8)) {
            Properties values = new Properties(); values.load(reader);
            return "1".equals(values.getProperty("schema"))
                    && "passed".equals(values.getProperty("status"))
                    && fingerprint.equals(values.getProperty("fingerprint"));
        } catch (Exception ignored) { return false; }
    }

    private static void store(Path proof, String stage, String fingerprint) throws Exception {
        Files.createDirectories(proof.getParent()); Properties values = new Properties();
        values.setProperty("schema", "1"); values.setProperty("status", "passed");
        values.setProperty("stage", stage); values.setProperty("fingerprint", fingerprint);
        values.setProperty("executed.at", Instant.now().toString());
        Path temporary = proof.resolveSibling(proof.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            values.store(writer, "Worldline verification stage PASS receipt");
        }
        try { Files.move(temporary, proof, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException error) { Files.move(temporary, proof); }
    }

    private String label(Path path) {
        return path.startsWith(root) ? root.relativize(path).toString().replace('\\', '/')
                : path.toString().replace('\\', '/');
    }
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }
    record Metrics(int restored, int executed) { }
}
