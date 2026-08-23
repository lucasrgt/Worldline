import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Loads the one bounded retention policy shared by every content cache. */
final class CachePolicy {
    private final long maximumBytes, minimumAgeMillis;
    CachePolicy(Path root) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(root.resolve("quality/cache-policy.properties"),
                StandardCharsets.UTF_8)) { values.load(reader); }
        require("1".equals(values.getProperty("schema")), "invalid shared cache policy schema");
        maximumBytes = environment("WORLDLINE_CACHE_MAX_GIB",
                Long.parseLong(required(values, "maximum.bytes")) >> 30, 1, 1024) << 30;
        minimumAgeMillis = TimeUnit.DAYS.toMillis(environment("WORLDLINE_CACHE_MAX_AGE_DAYS",
                Long.parseLong(required(values, "minimum.age.days")), 1, 3650));
    }
    long maximumBytes() { return maximumBytes; }
    long minimumAgeMillis() { return minimumAgeMillis; }
    private static long environment(String name, long fallback, long minimum, long maximum) {
        String raw = System.getenv(name); long value = fallback;
        if (raw != null && !raw.isBlank()) try { value = Long.parseLong(raw); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer"); }
        require(value >= minimum && value <= maximum, name + " is outside its safe range"); return value;
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && value.matches("[0-9]+"),
                "invalid cache policy " + key); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
