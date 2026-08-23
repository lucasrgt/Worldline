import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Prevents the reviewed scheduling aggregate from falling back to an empty schema. */
final class SmokeScheduleBaselineCheck {
    private SmokeScheduleBaselineCheck() { }

    static void execute(Path root) throws Exception {
        Properties lock = load(root.resolve("smokes/schedule-baseline.lock"));
        Properties schedule = load(root.resolve("smokes/schedule.properties"));
        require("1".equals(lock.getProperty("schema"))
                        && "2".equals(schedule.getProperty("schema")),
                "invalid smoke schedule baseline schema");
        int count = integer(lock, "sample.count"), checked = 0;
        String history = required(lock, "history.path");
        require(history.equals("tools/harness/SmokeScheduleHistory.java")
                        && digest(root.resolve(history)).equals(required(lock, "history.current.sha256"))
                        && required(lock, "history.prior.sha256").matches("[0-9a-f]{64}"),
                "smoke schedule implementation drift");
        for (String key : lock.stringPropertyNames()) {
            if (!key.startsWith("sample.") || !key.endsWith(".source.sha256")) continue;
            String id = key.substring(7, key.length() - 14); checked++;
            require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
                            && lock.getProperty(key).matches("[0-9a-f]{64}"),
                    "invalid schedule evidence row: " + key);
            for (String field : new String[] {"attempts", "failures", "duration.total.ms"})
                require(number(schedule, "smoke." + id + "." + field)
                                >= number(lock, "sample." + id + "." + field),
                        "smoke schedule baseline regressed: " + id + "/" + field);
        }
        require(count == 7 && checked == count, "smoke schedule baseline census drift");
        System.out.println("  smoke schedule baseline: 7 observed milestones");
    }

    static boolean transports(Path root, String relative, String prior) throws Exception {
        Properties lock = load(root.resolve("smokes/schedule-baseline.lock"));
        return relative.equals(lock.getProperty("history.path"))
                && prior.equals(lock.getProperty("history.prior.sha256"))
                && digest(root.resolve(relative)).equals(lock.getProperty("history.current.sha256"));
    }

    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static int integer(Properties values, String key) {
        return Math.toIntExact(number(values, key));
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(),
                "missing " + key); return value;
    }
    private static String digest(Path path) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
    private static long number(Properties values, String key) {
        String value = values.getProperty(key); require(value != null, "missing " + key);
        try { long parsed = Long.parseLong(value); require(parsed >= 0, "negative " + key); return parsed; }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
