import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Ratchets every raw recursive Files traversal in maintained verification tooling. */
final class FilesWalkPolicy {
    private static final Pattern CALL = Pattern.compile(
            "Files\\s*[.]\\s*(?:walk|walkFileTree)\\s*[(]");
    private FilesWalkPolicy() { }

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--write")), "usage: FilesWalkPolicy --write");
            Path root = Path.of("").toAbsolutePath().normalize(); write(root, census(root));
        } catch (Exception error) {
            System.err.println("Files traversal policy failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void execute(Path root) throws Exception {
        Path policyPath = root.resolve("quality/files-walk-allowlist.properties");
        Properties policy = StrictProperties.load(policyPath);
        require("2".equals(policy.remove("schema")), "invalid Files traversal allowlist schema");
        Map<String, Integer> actual = census(root);
        for (Map.Entry<String, Integer> row : actual.entrySet()) {
            String allowed = policy.getProperty(row.getKey());
            require(allowed != null, "unreviewed recursive Files traversal: " + row.getKey());
            require(Integer.toString(row.getValue()).equals(allowed),
                    "Files traversal allowance drift: " + row.getKey() + " has " + row.getValue()
                            + ", recorded " + allowed);
        }
        for (String relative : policy.stringPropertyNames())
            require(actual.containsKey(relative), "stale Files traversal allowance: " + relative);
        System.out.println("  raw Files traversal allowance: " + actual.size() + " files, "
                + actual.values().stream().mapToInt(Integer::intValue).sum() + " calls");
    }

    private static Map<String, Integer> census(Path root) throws Exception {
        Map<String, Integer> actual = new HashMap<>();
        String tracked = ProcessCapture.require(root,
                List.of("git", "ls-files", "tools/harness/*.java", "tools/integration/*.java",
                        "tools/testkit/*.java", "tools/smoke/*.java"), 60);
        for (String relative : tracked.lines().filter(value -> !value.isBlank()).toList()) {
            Matcher matcher = CALL.matcher(Files.readString(root.resolve(relative), StandardCharsets.UTF_8));
            int count = 0; while (matcher.find()) count++;
            if (count > 0) actual.put(relative.replace('\\', '/'), count);
        }
        return actual;
    }

    private static void write(Path root, Map<String, Integer> actual) throws Exception {
        StringBuilder output = new StringBuilder("# Reviewed recursive Files traversal debt\n")
                .append("schema=2\n");
        new TreeMap<>(actual).forEach((path, count) -> output.append(path).append('=')
                .append(count).append('\n'));
        Files.writeString(root.resolve("quality/files-walk-allowlist.properties"), output,
                StandardCharsets.UTF_8);
        System.out.println("Files traversal allowlist written: " + actual.size() + " files");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
