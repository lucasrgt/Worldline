import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fast pre-push validation of an orchestrator receipt for one exact Git SHA. */
public final class PushCheck {
    private PushCheck() {}

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 2) throw new IllegalArgumentException(
                    "usage: java tools/harness/PushCheck.java LOCAL_SHA REMOTE_REF");
            verify(Path.of("").toAbsolutePath().normalize(), arguments[0], arguments[1]);
            System.out.println("pre-push: orchestrator authorization matches " + shortSha(arguments[0]));
        } catch (Exception error) {
            System.err.println("pre-push blocked: " + error.getMessage()); System.exit(1);
        }
    }

    static void verify(Path root, String sha, String remoteRef) throws Exception {
        require(sha.matches("[0-9a-f]{40,64}"), "invalid local SHA");
        require(remoteRef.matches("refs/heads/[A-Za-z0-9._/-]+"), "invalid remote branch");
        Path receipt = root.resolve(".worldline/reports/orchestrator-push.json");
        Path plan = root.resolve(".worldline/reports/integration-plan.json");
        require(Files.isRegularFile(receipt), "run Gate.java --orchestrator before pushing");
        require(Files.isRegularFile(plan), "qualified integration plan disappeared");
        String json = Files.readString(receipt, StandardCharsets.UTF_8);
        require("passed".equals(field(json, "status")), "orchestrator receipt did not pass");
        require(sha.equals(field(json, "head")), "orchestrator receipt belongs to another commit");
        require(digest(plan).equals(field(json, "integration_plan_sha256")),
                "integration plan changed after orchestrator qualification");
    }

    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static String field(String json, String name) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(name)
                + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        require(matcher.find(), "orchestrator receipt is missing " + name); return matcher.group(1);
    }

    private static String shortSha(String value) { return value.substring(0, Math.min(12, value.length())); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
