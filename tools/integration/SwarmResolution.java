import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts abandoned legacy outcomes into owned RETRYABLE or evidence-bound REJECTED states. */
final class SwarmResolution {
    private static final Pattern OBJECT = Pattern.compile("\\{\"id\":\"(m[0-9]+-[^\"]+)\"([^}]*)}");
    private static final Pattern FIELD = Pattern.compile("\"([^\"]+)\":(?:\"([^\"]*)\"|([^,}]+))");
    private SwarmResolution() { }

    static void write(Path censusValue, String scar, String rejectedValue, int maxAttempts)
            throws Exception {
        require(scar.matches("NYA-[A-Z0-9]+"), "invalid NYA scar: " + scar);
        require(maxAttempts > 0, "max attempts must be positive");
        Path root = Path.of("").toAbsolutePath().normalize();
        Path census = censusValue.toAbsolutePath().normalize();
        require(Files.isRegularFile(census), "missing census: " + census);
        Set<String> rejected = rejectedValue.isBlank() ? Set.of()
                : Set.of(rejectedValue.split(","));
        List<Row> rows = rows(Files.readString(census, StandardCharsets.UTF_8));
        Path directory = root.resolve("coordination/swarm/dispositions");
        Files.createDirectories(directory);
        Set<String> found = new HashSet<>(); int retryable = 0, rejectedCount = 0;
        String now = Instant.now().toString();
        for (Row row : rows) {
            if (!Set.of("DIRTY_SUSPENDED", "FAILED_GATE").contains(row.get("state"))) continue;
            require(!row.get("archive").isBlank() && row.get("archive_sha256").matches("[0-9a-f]{64}"),
                    "unarchived unresolved worker: " + row.id);
            boolean reject = rejected.contains(row.id); found.add(row.id);
            String state = reject ? "REJECTED" : "RETRYABLE";
            if (reject) rejectedCount++; else retryable++;
            Path output = directory.resolve(row.id + ".properties");
            require(!Files.exists(output), "disposition already exists: " + output);
            String next = reject ? "archive-do-not-integrate" : "resume-same-session-and-worktree";
            String text = "schema=1\nid=" + row.id + "\ndisposition=" + state + "\nprior.state="
                    + row.get("state") + "\nbranch=" + row.get("branch") + "\nworktree="
                    + slash(row.get("path")) + "\nbase=" + row.get("base") + "\nhead="
                    + row.get("head") + "\ntree=" + row.get("tree") + "\ncause="
                    + property(row.get("cause")) + "\nscar=" + scar + "\nattempt=1\nmax.attempts="
                    + maxAttempts + "\narchive=" + slash(row.get("archive")) + "\narchive.sha256="
                    + row.get("archive_sha256") + "\nnext.action=" + next + "\ncreated=" + now + "\n";
            Files.writeString(output, text, StandardCharsets.UTF_8);
        }
        require(found.containsAll(rejected), "rejected ID was not unresolved: " + difference(rejected, found));
        Path report = root.resolve(".worldline/reports/swarm-resolution.json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, "{\n  \"schema\":1,\n  \"created\":\"" + now
                + "\",\n  \"scar\":\"" + scar + "\",\n  \"retryable\":" + retryable
                + ",\n  \"rejected\":" + rejectedCount + ",\n  \"stranded\":0\n}\n",
                StandardCharsets.UTF_8);
        System.out.println("swarm resolution: retryable=" + retryable + ", rejected="
                + rejectedCount + ", stranded=0");
        System.out.println("  report: " + report);
    }

    private static List<Row> rows(String json) {
        List<Row> result = new ArrayList<>(); Matcher objects = OBJECT.matcher(json);
        while (objects.find()) {
            Map<String, String> fields = new HashMap<>(); fields.put("id", objects.group(1));
            Matcher matcher = FIELD.matcher(objects.group(2));
            while (matcher.find()) fields.put(matcher.group(1), unescape(
                    matcher.group(2) == null ? matcher.group(3).trim() : matcher.group(2)));
            result.add(new Row(objects.group(1), fields));
        }
        return result;
    }

    private static String unescape(String value) {
        return value.replace("\\r", "\r").replace("\\n", "\n")
                .replace("\\\"", "\"").replace("\\\\", "\\");
    }
    private static String property(String value) {
        return value.replace("\\", "\\\\").replace("\r", " ").replace("\n", " ")
                .replace("=", "\\=").replace(":", "\\:");
    }
    private static String slash(String value) { return value.replace('\\', '/'); }
    private static Set<String> difference(Set<String> expected, Set<String> actual) {
        Set<String> result = new HashSet<>(expected); result.removeAll(actual); return result;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private record Row(String id, Map<String, String> fields) {
        String get(String key) { return fields.getOrDefault(key, ""); }
    }
}
