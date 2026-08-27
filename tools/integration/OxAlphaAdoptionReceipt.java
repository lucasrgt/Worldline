import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Strict typed view of one immutable legacy-retry adoption receipt. */
record OxAlphaAdoptionReceipt(int schema, String id, String mode, String controlBase,
        String branch, Path worktree, int priorAttempt, int authorizedAttempt, int maxAttempts,
        String archiveSha, String session, int recoverySessions, int recoveryTimeout, String status) {
    static OxAlphaAdoptionReceipt read(Path path) throws Exception {
        Map<String, Object> json = MiniJson.object(Files.readString(path, StandardCharsets.UTF_8));
        return new OxAlphaAdoptionReceipt(exactInt(json, "schema"), MiniJson.string(json, "id"),
                MiniJson.string(json, "mode"), MiniJson.string(json, "new_control_base"),
                MiniJson.string(json, "branch"),
                Path.of(MiniJson.string(json, "worktree")).toAbsolutePath().normalize(),
                exactInt(json, "prior_attempt"), exactInt(json, "authorized_attempt"),
                exactInt(json, "max_attempts"), MiniJson.string(json, "archive_sha256"),
                nullableString(json, "session"),
                exactInt(json, "recovery_sessions_allowed"),
                exactInt(json, "recovery_timeout_seconds"), MiniJson.string(json, "status"));
    }

    private static String nullableString(Map<String, Object> json, String name) {
        Object value = json.get(name);
        require(json.containsKey(name) && (value == null || value instanceof String),
                name + " is not a nullable string");
        return (String) value;
    }

    private static int exactInt(Map<String, Object> json, String name) {
        long value = MiniJson.integer(json, name);
        require(value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE,
                name + " is outside the integer range");
        return (int) value;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
