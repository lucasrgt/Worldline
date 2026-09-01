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
    private Boolean cleanTree;
    private Map<String, String> trackedObjects;

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
            CacheUsage.touch(proof);
            if (countMetrics) restoredStages++;
            System.out.println("  verification stage restored: " + stage);
            return;
        }
        action.run(); store(proof, stage, fingerprint); CacheUsage.touch(proof);
        if (countMetrics) executedStages++;
    }

    void executeDirectory(String stage, List<Path> inputs, Path output,
            VerifyReport.Checked action) throws Exception {
        String fingerprint = fingerprint(stage, inputs);
        Path object = objects.resolve(stage).resolve(fingerprint);
        Path proof = object.resolve("proof.properties"), snapshot = object.resolve("output");
        if (valid(proof, fingerprint) && Files.isDirectory(snapshot)) {
            restore(snapshot, output); CacheUsage.touch(proof);
            if (countMetrics) restoredStages++;
            System.out.println("  verification artifact restored: " + stage); return;
        }
        if (Files.exists(output, java.nio.file.LinkOption.NOFOLLOW_LINKS)) SafeTreeDelete.delete(output);
        action.run(); require(Files.isDirectory(output), "stage produced no output: " + stage);
        Path temporary = object.resolveSibling(object.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        copy(output, temporary.resolve("output"));
        store(temporary.resolve("proof.properties"), stage, fingerprint);
        try { Files.move(temporary, object, StandardCopyOption.ATOMIC_MOVE); }
        catch (java.nio.file.FileAlreadyExistsException ignored) { SafeTreeDelete.delete(temporary); }
        catch (AtomicMoveNotSupportedException error) {
            try { Files.move(temporary, object); }
            catch (java.nio.file.FileAlreadyExistsException ignored) { SafeTreeDelete.delete(temporary); }
        }
        CacheUsage.touch(object.resolve("proof.properties"));
        if (countMetrics) executedStages++;
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
            Path readme = root.resolve("README.md"); Files.writeString(readme, "status one\n");
            Path docs = root.resolve("docs/status.md"); Files.createDirectories(docs.getParent());
            Files.writeString(docs, "docs one\n");
            Path qualification = root.resolve("smokes/qualification.lock");
            Files.createDirectories(qualification.getParent()); Files.writeString(qualification, "pins one\n");
            List<Path> policyInputs = new RepositoryStageInputs(root).sourcePolicy();
            int[] policyExecutions = {0}; VerifyReport.Checked policy = () -> policyExecutions[0]++;
            new VerificationStageCache(root, root.resolve("objects"), false)
                    .execute("source-policy-test", policyInputs, policy);
            new VerificationStageCache(root, root.resolve("objects"), false)
                    .execute("source-policy-test", policyInputs, policy);
            Files.writeString(readme, "status two\n");
            new VerificationStageCache(root, root.resolve("objects"), false)
                    .execute("source-policy-test", policyInputs, policy);
            Files.writeString(qualification, "pins two\n");
            new VerificationStageCache(root, root.resolve("objects"), false)
                    .execute("source-policy-test", policyInputs, policy);
            require(policyExecutions[0] == 3,
                    "source-policy cache did not invalidate for README or qualification drift");
            Path directoryInput = root.resolve("directory-input.txt");
            Files.writeString(directoryInput, "one\n");
            Path directoryOutput = root.resolve("directory-output"); int[] directoryExecutions = {0};
            VerifyReport.Checked directoryAction = () -> {
                directoryExecutions[0]++; Files.createDirectories(directoryOutput);
                Files.writeString(directoryOutput.resolve("value.txt"),
                        Files.readString(directoryInput));
            };
            VerificationStageCache directoryCache = new VerificationStageCache(
                    root, root.resolve("objects"), false);
            directoryCache.executeDirectory("directory-test", List.of(directoryInput),
                    directoryOutput, directoryAction);
            directoryCache.executeDirectory("directory-test", List.of(directoryInput),
                    directoryOutput, directoryAction);
            require(directoryExecutions[0] == 1
                            && "one\n".equals(Files.readString(directoryOutput.resolve("value.txt"))),
                    "directory stage did not restore its immutable output");
            Files.writeString(directoryOutput.resolve("value.txt"), "tampered\n");
            directoryCache.executeDirectory("directory-test", List.of(directoryInput),
                    directoryOutput, directoryAction);
            require(directoryExecutions[0] == 1
                            && "one\n".equals(Files.readString(directoryOutput.resolve("value.txt"))),
                    "directory stage accepted a modified output");
            Files.writeString(directoryInput, "two\n");
            new VerificationStageCache(root, root.resolve("objects"), false).executeDirectory(
                    "directory-test", List.of(directoryInput), directoryOutput, directoryAction);
            require(directoryExecutions[0] == 2
                            && "two\n".equals(Files.readString(directoryOutput.resolve("value.txt"))),
                    "directory stage did not invalidate its immutable output");
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
        String tracked = trackedDigest(input);
        if (tracked != null) { inputDigests.put(input, tracked); return tracked; }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        if (!Files.exists(input)) update(digest, "missing");
        else if (Files.isRegularFile(input)) digest.update(Files.readAllBytes(input));
        else {
            List<Path> paths = SafeTreeDelete.paths(input);
            for (Path path : paths.stream().sorted(Comparator.naturalOrder()).toList()) {
                if (SafeTreeDelete.linkLike(path))
                    throw new java.io.IOException("verification input contains a filesystem link: " + path);
                if (!Files.isRegularFile(path)) continue;
                update(digest, input.relativize(path).toString().replace('\\', '/'));
                digest.update(Files.readAllBytes(path));
            }
        }
        String value = HexFormat.of().formatHex(digest.digest()); inputDigests.put(input, value);
        return value;
    }

    private String trackedDigest(Path input) throws Exception {
        if (!input.startsWith(root)) return null;
        if (cleanTree == null) {
            String status = capture(List.of("git", "status", "--porcelain", "--untracked-files=all"));
            cleanTree = status != null && status.isBlank();
        }
        if (!cleanTree) return null;
        String relative = root.relativize(input).toString().replace('\\', '/');
        if (trackedObjects == null) trackedObjects = trackedObjects();
        String object = trackedObjects.get(relative);
        return object == null ? null : "git:" + object;
    }

    private Map<String, String> trackedObjects() throws Exception {
        String output = capture(List.of("git", "ls-tree", "-r", "-t", "--full-tree", "HEAD"));
        require(output != null, "git tree inventory failed"); Map<String, String> result = new HashMap<>();
        for (String line : output.lines().toList()) {
            int tab = line.indexOf('\t'); if (tab < 0) continue;
            String[] metadata = line.substring(0, tab).split(" ");
            if (metadata.length == 3 && metadata[2].matches("[0-9a-f]{40,64}"))
                result.put(line.substring(tab + 1), metadata[2]);
        }
        return Map.copyOf(result);
    }

    private String capture(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return process.waitFor() == 0 ? output : null;
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

    private static void restore(Path snapshot, Path output) throws Exception {
        if (Files.isDirectory(output) && directoryDigest(snapshot).equals(directoryDigest(output))) return;
        if (Files.exists(output, java.nio.file.LinkOption.NOFOLLOW_LINKS)) SafeTreeDelete.delete(output);
        copy(snapshot, output);
    }

    private static String directoryDigest(Path directory) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (Path path : SafeTreeDelete.paths(directory).stream().sorted().toList()) {
            require(!SafeTreeDelete.linkLike(path),
                    "verification artifact contains a filesystem link: " + path);
            Path relative = directory.relativize(path);
            if (relative.toString().isEmpty()) continue;
            update(digest, relative.toString().replace('\\', '/'));
            update(digest, Files.isDirectory(path) ? "directory" : "file");
            if (Files.isRegularFile(path)) digest.update(Files.readAllBytes(path));
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void copy(Path source, Path target) throws Exception {
        Files.createDirectories(target);
        for (Path path : SafeTreeDelete.paths(source)) {
            require(!SafeTreeDelete.linkLike(path), "cached stage contains a filesystem link: " + path);
            Path destination = target.resolve(source.relativize(path).toString());
            if (Files.isDirectory(path)) Files.createDirectories(destination);
            else if (Files.isRegularFile(path)) {
                Files.createDirectories(destination.getParent());
                Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private String label(Path path) {
        return path.startsWith(root) ? root.relativize(path).toString().replace('\\', '/')
                : path.toString().replace('\\', '/');
    }
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Metrics(int restored, int executed) { }
}
