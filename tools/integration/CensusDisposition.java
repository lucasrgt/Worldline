import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Resolves terminal rejected dispositions independently from a wave's current control base. */
final class CensusDisposition {
    private CensusDisposition() {
    }

    static Map<String, Decision> load(Path root) throws Exception {
        Map<String, Decision> result = new HashMap<>();
        for (RejectionRegistry.Entry entry : RejectionRegistry.load(root, null)) {
            Path path = root.resolve("coordination/swarm/dispositions/" + entry.id() + ".properties");
            Properties values = properties(path);
            Path worktree = Path.of(required(values, "worktree")).toAbsolutePath().normalize();
            Path archive = Path.of(required(values, "archive")).toAbsolutePath().normalize();
            String archiveSha = required(values, "archive.sha256").toLowerCase();
            require(Files.isRegularFile(archive)
                    && archiveSha.equals(SwarmEvidenceArchive.sha256(archive).toLowerCase()),
                    "rejected archive drift: " + entry.id());
            String head = first(values, "head", "failure.head");
            String tree = first(values, "tree", "failure.tree");
            Decision decision = new Decision("REJECTED", required(values, "branch"), worktree,
                    required(values, "base"), head, tree, required(values, "cause"),
                    new SwarmEvidenceArchive.Result(archive.toString(), archiveSha));
            require(result.put(entry.id(), decision) == null,
                    "duplicate census disposition: " + entry.id());
        }
        return Map.copyOf(result);
    }

    private static Properties properties(Path path) throws Exception {
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static String first(Properties values, String... keys) {
        for (String key : keys) {
            String value = values.getProperty(key, "").trim();
            if (!value.isBlank()) {
                return value;
            }
        }
        throw new IllegalStateException("missing disposition identity: " + List.of(keys));
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

    record Decision(String state, String branch, Path worktree, String base, String head,
            String tree, String cause, SwarmEvidenceArchive.Result archive) {
        void requireExact(Path actualWorktree, String actualBranch) throws Exception {
            require(worktree.equals(actualWorktree) && branch.equals(actualBranch),
                    "rejected worktree identity drift: " + actualWorktree.getFileName());
            String archivedTree = SwarmProcess.output(actualWorktree,
                    List.of("rev-parse", head + "^{tree}"), 60).trim();
            require(tree.equals(archivedTree),
                    "rejected archived tree drift: " + actualWorktree.getFileName());
        }
    }
}
