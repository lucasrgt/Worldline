import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One-time migration of a complete pre-cache smoke report into immutable PASS proofs. */
public final class SmokeLegacyImport {
    private static final Pattern STARTED = Pattern.compile("\\\"started\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern PROFILE = Pattern.compile("\\\"profile\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern STATUS = Pattern.compile("\\\"status\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern FAILED = Pattern.compile(
            "smoke suite failed: ([a-z0-9]+(?:-[a-z0-9]+)*) (?:exited|timed out)");

    private SmokeLegacyImport() {}

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 0) throw new IllegalArgumentException(
                    "usage: java -cp <new-harness-classes> SmokeLegacyImport");
            Path root = Path.of("").toAbsolutePath().normalize(); importReport(root);
        } catch (Exception error) {
            System.err.println("legacy smoke import failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void importReport(Path root) throws Exception {
        SmokeGitState state = SmokeGitState.read(root);
        require(state.clean(), "legacy smoke import requires a clean worktree");
        Path report = root.resolve(".worldline/reports/verify.json");
        require(Files.isRegularFile(report), "missing completed legacy verify report");
        String json = Files.readString(report, StandardCharsets.UTF_8);
        require("smoke".equals(first(PROFILE, json, "profile")), "legacy report is not a smoke profile");
        String status = first(STATUS, json, "status");
        require(status.equals("passed") || status.equals("failed"), "invalid legacy smoke status");
        Matcher matcher = STARTED.matcher(json); require(matcher.find(), "legacy report has no start time");
        Instant started = Instant.parse(matcher.group(1));
        List<SmokeDiscovery.Entry> smokes = SmokeDiscovery.discover(root);
        int limit = smokes.size();
        if (status.equals("failed")) {
            Matcher failed = FAILED.matcher(json);
            require(failed.find(), "failed legacy report has no identified smoke boundary");
            String id = failed.group(1); limit = 0;
            while (limit < smokes.size() && !smokes.get(limit).id.equals(id)) limit++;
            require(limit < smokes.size(), "legacy failure names an unknown smoke: " + id);
        } else require(json.matches("(?s).*\\\"name\\\"\\s*:\\s*\\\"smokes\\\"\\s*,"
                + "\\s*\\\"status\\\"\\s*:\\s*\\\"passed\\\".*"),
                "legacy report has no completed smoke stage");
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        for (int index = 0; index < limit; index++) {
            SmokeDiscovery.Entry smoke = smokes.get(index);
            Path log = root.resolve(".worldline/smoke-logs").resolve(smoke.id + ".log");
            require(Files.isRegularFile(log) && !Files.getLastModifiedTime(log).toInstant().isBefore(started),
                    "missing fresh legacy PASS log: " + smoke.id);
            require(json.contains("smoke " + smoke.id + ":"),
                    "legacy report does not attest completed smoke: " + smoke.id);
            cache.passed(smoke, cache.fingerprint(smoke), 0L);
        }
        if (limit == smokes.size()) cache.finish(smokes.size());
        System.out.println("legacy smoke PASS proofs imported: " + limit
                + " @ " + state.head().substring(0, 12));
    }

    private static String first(Pattern pattern, String text, String name) {
        Matcher matcher = pattern.matcher(text); require(matcher.find(), "legacy report has no " + name);
        return matcher.group(1);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
