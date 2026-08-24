import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Ratchets every remaining raw Files.walk call in maintained coordination tooling. */
final class FilesWalkPolicy {
    private static final Pattern CALL = Pattern.compile("Files\\s*[.]\\s*walk\\s*[(]");
    private FilesWalkPolicy() { }

    static void execute(Path root) throws Exception {
        Path policyPath = root.resolve("quality/files-walk-allowlist.properties");
        Properties policy = StrictProperties.load(policyPath);
        require("1".equals(policy.remove("schema")), "invalid Files.walk allowlist schema");
        Map<String, Integer> actual = new HashMap<>();
        String tracked = ProcessCapture.require(root,
                java.util.List.of("git", "ls-files", "tools/harness/*.java", "tools/integration/*.java"), 60);
        for (String relative : tracked.lines().filter(value -> !value.isBlank()).toList()) {
            Matcher matcher = CALL.matcher(Files.readString(root.resolve(relative), StandardCharsets.UTF_8));
            int count = 0; while (matcher.find()) count++;
            if (count > 0) actual.put(relative.replace('\\', '/'), count);
        }
        for (Map.Entry<String, Integer> row : actual.entrySet()) {
            String allowed = policy.getProperty(row.getKey());
            require(allowed != null, "unreviewed Files.walk call: " + row.getKey());
            require(Integer.toString(row.getValue()).equals(allowed),
                    "Files.walk allowance drift: " + row.getKey() + " has " + row.getValue()
                            + ", recorded " + allowed);
        }
        for (String relative : policy.stringPropertyNames())
            require(actual.containsKey(relative), "stale Files.walk allowance: " + relative);
        System.out.println("  raw Files.walk allowance: " + actual.size() + " files, "
                + actual.values().stream().mapToInt(Integer::intValue).sum() + " calls");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
