import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Rejects new numeric milestone collisions while preserving reviewed legacy namespaces. */
final class MilestoneIdUniquenessCheck {
    private static final Pattern SMOKE = Pattern.compile("^m([0-9]+)-[a-z0-9-]+$");
    private static final Pattern DOC = Pattern.compile("^M([0-9]+)_[A-Z0-9_]+[.]md$");
    private MilestoneIdUniquenessCheck() { }

    static void execute(Path root) throws Exception {
        Properties reviewed = load(root.resolve("quality/milestone-id-collisions.properties"));
        require("1".equals(reviewed.getProperty("schema")), "invalid milestone collision policy");
        Map<String, List<String>> smokes = new TreeMap<>();
        try (var paths = Files.list(root.resolve("smokes"))) {
            for (Path path : paths.filter(Files::isDirectory).toList()) {
                Matcher match = SMOKE.matcher(path.getFileName().toString());
                if (!match.matches() || empty(path)) continue;
                smokes.computeIfAbsent(match.group(1), ignored ->
                        new ArrayList<>()).add(path.getFileName().toString());
            }
        }
        int collisions = 0;
        for (Map.Entry<String, List<String>> entry : smokes.entrySet()) {
            List<String> names = entry.getValue().stream().sorted().toList();
            if (names.size() == 1) require(reviewed.getProperty("m" + entry.getKey()) == null,
                    "stale milestone collision allowance: m" + entry.getKey());
            else {
                collisions++; require(String.join(",", names).equals(
                                reviewed.getProperty("m" + entry.getKey())),
                        "unreviewed numeric milestone collision: m" + entry.getKey());
            }
        }
        int documents = 0, historicalDocuments = 0;
        try (var paths = Files.list(root.resolve("docs"))) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                Matcher match = DOC.matcher(path.getFileName().toString());
                if (!match.matches()) continue;
                documents++;
                if (!smokes.containsKey(match.group(1))) historicalDocuments++;
            }
        }
        require(reviewed.stringPropertyNames().stream().filter(key -> key.startsWith("m")).count()
                        == collisions, "milestone collision policy census drift");
        System.out.println("  milestone IDs: " + smokes.size() + " numeric namespaces, "
                + collisions + " reviewed legacy collisions, " + documents + " documents ("
                + historicalDocuments + " pre-descriptor)");
    }

    /** An interrupted probe leaves directories without files; only a file claims a namespace. */
    private static boolean empty(Path directory) throws Exception {
        for (Path path : SafeTreeDelete.paths(directory)) {
            if (Files.isRegularFile(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                return false;
            }
        }
        return true;
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-milestone-id-");
        try {
            Files.createDirectories(root.resolve("docs"));
            Files.createDirectories(root.resolve("quality"));
            Files.writeString(root.resolve("quality/milestone-id-collisions.properties"),
                    "schema=1\n", StandardCharsets.UTF_8);
            Path claimed = root.resolve("smokes/m1-alpha");
            Files.createDirectories(claimed);
            Files.writeString(claimed.resolve("smoke.properties"), "id=m1-alpha\n",
                    StandardCharsets.UTF_8);
            Files.createDirectories(root.resolve("smokes/m1-orphan-probe/nested/empty"));
            execute(root);
            Files.writeString(root.resolve("smokes/m1-orphan-probe/smoke.properties"),
                    "id=m1-orphan-probe\n", StandardCharsets.UTF_8);
            try {
                execute(root);
                throw new IllegalStateException("unreviewed milestone collision passed");
            } catch (IllegalStateException expected) {
                require(expected.getMessage().contains("unreviewed numeric milestone collision"),
                        "unexpected collision failure: " + expected.getMessage());
            }
        } finally {
            SafeTreeDelete.delete(root);
        }
    }

    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
