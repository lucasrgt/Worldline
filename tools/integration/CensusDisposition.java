import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Resolves every explicit retryable or rejected disposition independently from wave bases. */
final class CensusDisposition {
    private static final Set<String> EXPLICIT = Set.of("RETRYABLE", "REJECTED");
    private CensusDisposition() {
    }

    static Map<String, Decision> load(Path root) throws Exception {
        Set<String> registered = Set.copyOf(RejectionRegistry.load(root, null).stream()
                .map(RejectionRegistry.Entry::id).toList());
        Map<String, Decision> result = loadExplicit(root);
        require(result.keySet().containsAll(registered),
                "rejection registry references a missing explicit disposition");
        return result;
    }

    private static Map<String, Decision> loadExplicit(Path root) throws Exception {
        Path directory = root.resolve("coordination/swarm/dispositions");
        require(Files.isDirectory(directory), "missing census disposition directory");
        Map<String, Decision> result = new HashMap<>();
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString)).toList()) {
                Properties values = properties(path);
                String state = values.getProperty("disposition", "").trim();
                require(state.isBlank() || EXPLICIT.contains(state) || "QUALIFIED".equals(state),
                        "invalid explicit disposition: " + path.getFileName());
                if (!EXPLICIT.contains(state)) {
                    continue;
                }
                Decision decision = decision(path, values, state);
                require(result.put(decision.id(), decision) == null,
                        "duplicate census disposition: " + decision.id());
            }
        }
        return Map.copyOf(result);
    }

    private static Decision decision(Path path, Properties values, String state) throws Exception {
        String id = required(values, "id");
        require(path.getFileName().toString().equals(id + ".properties"),
                "disposition filename/id drift: " + id);
        require("1".equals(required(values, "schema")), "invalid disposition schema: " + id);
        Path worktree = Path.of(required(values, "worktree")).toAbsolutePath().normalize();
        Path archive = Path.of(required(values, "archive")).toAbsolutePath().normalize();
        String archiveSha = required(values, "archive.sha256").toLowerCase();
        require(archiveSha.matches("[0-9a-f]{64}") && Files.isRegularFile(archive)
                && archiveSha.equals(SwarmEvidenceArchive.sha256(archive).toLowerCase()),
                "disposition archive drift: " + id);
        String base = requiredSha(values, "base", id);
        String head = firstSha(values, id, "head", "failure.head");
        String tree = firstSha(values, id, "tree", "failure.tree");
        int attempt = integer(values, "attempt", id);
        int maximum = integer(values, "max.attempts", id);
        String owner = values.getProperty("owner", "").trim();
        String session = values.getProperty("session", "").trim();
        if ("RETRYABLE".equals(state)) {
            require(!owner.isBlank(), "missing RETRYABLE owner: " + id);
            require(!session.isBlank(), "missing RETRYABLE session: " + id);
            require(attempt > 0 && attempt < maximum,
                    "RETRYABLE attempt budget exhausted or invalid: " + id);
        }
        return new Decision(id, state, required(values, "branch"), worktree, base, head, tree,
                required(values, "cause"), owner, session, attempt, maximum,
                new SwarmEvidenceArchive.Result(archive.toString(), archiveSha));
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-census-disposition-");
        Path directory = root.resolve("coordination/swarm/dispositions");
        Files.createDirectories(directory);
        Path archive = root.resolve("attempt.zip");
        Files.writeString(archive, "preserved evidence", StandardCharsets.UTF_8);
        String sha = SwarmEvidenceArchive.sha256(archive);
        Path retry = directory.resolve("m1-retry.properties");
        Files.writeString(retry, fixture("m1-retry", "RETRYABLE", archive, sha)
                + "owner=worldline-orchestrator\nsession=ses_exact\nattempt=1\nmax.attempts=2\n",
                StandardCharsets.UTF_8);
        Path rejected = directory.resolve("m2-rejected.properties");
        Files.writeString(rejected, fixture("m2-rejected", "REJECTED", archive, sha)
                + "attempt=2\nmax.attempts=2\n", StandardCharsets.UTF_8);
        Map<String, Decision> valid = loadExplicit(root);
        require(valid.size() == 2 && "ses_exact".equals(valid.get("m1-retry").session()),
                "explicit RETRYABLE and REJECTED dispositions were not both loaded");
        Files.writeString(retry, fixture("m1-retry", "RETRYABLE", archive, sha)
                + "owner=worldline-orchestrator\nattempt=1\nmax.attempts=2\n",
                StandardCharsets.UTF_8);
        expectFailure(root, "missing RETRYABLE session");
        Files.writeString(retry, fixture("m1-retry", "RETRYABLE", archive, sha)
                + "owner=worldline-orchestrator\nsession=ses_exact\nattempt=2\nmax.attempts=2\n",
                StandardCharsets.UTF_8);
        expectFailure(root, "RETRYABLE attempt budget exhausted or invalid");
        Files.writeString(retry, fixture("m1-retry", "RETRYABLE", archive, "0".repeat(64))
                + "owner=worldline-orchestrator\nsession=ses_exact\nattempt=1\nmax.attempts=2\n",
                StandardCharsets.UTF_8);
        expectFailure(root, "disposition archive drift");
        Files.delete(retry);
        Files.delete(rejected);
        Files.delete(archive);
        Files.delete(directory);
        Files.delete(directory.getParent());
        Files.delete(root.resolve("coordination"));
        Files.delete(root);
    }

    private static String fixture(String id, String state, Path archive, String sha) {
        String normalized = archive.toString().replace('\\', '/');
        return "schema=1\nid=" + id + "\ndisposition=" + state
                + "\nbranch=codex/milestone-" + id + "\nworktree=" + normalized
                + "\nbase=" + "1".repeat(40) + "\nhead=" + "2".repeat(40)
                + "\ntree=" + "3".repeat(40) + "\ncause=self-test\narchive=" + normalized
                + "\narchive.sha256=" + sha + "\n";
    }

    private static void expectFailure(Path root, String message) throws Exception {
        boolean failed = false;
        try {
            loadExplicit(root);
        } catch (IllegalStateException expected) {
            require(expected.getMessage().contains(message),
                    "unexpected disposition failure: " + expected.getMessage());
            failed = true;
        }
        require(failed, "invalid disposition passed: " + message);
    }

    private static Properties properties(Path path) throws Exception {
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static String firstSha(Properties values, String id, String... keys) {
        for (String key : keys) {
            String value = values.getProperty(key, "").trim();
            if (!value.isBlank()) {
                require(value.matches("[0-9a-f]{40}"), "invalid disposition " + key + ": " + id);
                return value;
            }
        }
        throw new IllegalStateException("missing disposition identity: " + List.of(keys));
    }
    private static String requiredSha(Properties values, String key, String id) {
        String value = required(values, key);
        require(value.matches("[0-9a-f]{40}"), "invalid disposition " + key + ": " + id);
        return value;
    }
    private static int integer(Properties values, String key, String id) {
        try {
            return Integer.parseInt(required(values, key));
        } catch (NumberFormatException error) {
            throw new IllegalStateException("invalid disposition " + key + ": " + id, error);
        }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing disposition " + key);
        return value.trim();
    }
    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

    record Decision(String id, String state, String branch, Path worktree, String base, String head,
            String tree, String cause, String owner, String session, int attempt, int maximum,
            SwarmEvidenceArchive.Result archive) {
        void requireExact(Path actualWorktree, String actualBranch) throws Exception {
            require(worktree.equals(actualWorktree) && branch.equals(actualBranch),
                    "disposition worktree identity drift: " + actualWorktree.getFileName());
            String archivedTree = SwarmProcess.output(actualWorktree,
                    List.of("rev-parse", head + "^{tree}"), 60).trim();
            require(tree.equals(archivedTree),
                    "disposition archived tree drift: " + actualWorktree.getFileName());
        }
    }
}
