import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Verifies a legacy adoption receipt at the Ox Alpha process boundary. */
final class OxAlphaLegacyAdoption {
    private OxAlphaLegacyAdoption() {
    }

    static void validate(Path root, OxAlphaRequest request) throws Exception {
        require(request.phase().equals("checkpoint") && request.attempt() == 2,
                "legacy adoption only authorizes checkpoint attempt 2");
        Path expected = root.resolve(".worldline/reports/swarm/legacy-retry-adoption-"
                + request.id() + ".json").normalize();
        Path supplied = Path.of(request.adoptionReceipt()).toAbsolutePath().normalize();
        require(supplied.equals(expected) && Files.isRegularFile(expected),
                "legacy adoption receipt is not at its canonical path");
        require(sha(expected).equalsIgnoreCase(request.adoptionSha()),
                "legacy adoption receipt SHA-256 drifted");
        String text = Files.readString(expected, StandardCharsets.UTF_8);
        require(field(text, "id", request.id())
                && field(text, "new_control_base", request.controlBase())
                && field(text, "branch", "codex/milestone-" + request.id())
                && field(text, "worktree", root.toString())
                && text.contains("\"prior_attempt\":1")
                && text.contains("\"authorized_attempt\":2")
                && text.contains("\"max_attempts\":2") && field(text, "status", "PASS"),
                "legacy adoption receipt drifted");
        if (request.session() == null) {
            require(field(text, "mode", "process-recovery") && text.contains("\"session\":null")
                    && text.contains("\"recovery_sessions_allowed\":1"),
                    "process-recovery adoption drifted");
            int limit = integer(text, "recovery_timeout_seconds");
            require(request.timeoutSeconds() > 0 && request.timeoutSeconds() <= limit && limit <= 3600,
                    "process-recovery session exceeds its adoption budget");
        } else {
            require(field(text, "mode", "resume-session")
                    && field(text, "session", request.session())
                    && text.contains("\"recovery_sessions_allowed\":0"),
                    "resume-session adoption drifted");
        }
    }

    private static boolean field(String json, String name, String value) {
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
        return json.contains("\"" + name + "\":\"" + escaped + "\"");
    }

    private static int integer(String json, String name) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(name) + "\\\":([0-9]+)")
                .matcher(json);
        require(matcher.find(), "legacy adoption receipt lacks " + name);
        return Integer.parseInt(matcher.group(1));
    }

    private static String sha(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
