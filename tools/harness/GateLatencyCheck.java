import java.io.Reader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Enforces versioned hot/cold gate latency SLOs and their measured trend. */
final class GateLatencyCheck {
    private GateLatencyCheck() { }

    static String enforce(Path root, long elapsedMillis) throws IOException {
        VerificationStageCache.Metrics metrics = VerificationStageCache.metrics();
        if (metrics.restored() + metrics.executed() == 0) return "not-applicable";
        Properties values = new Properties();
        Path policy = root.resolve("quality/gate-latency.properties");
        try (Reader reader = Files.newBufferedReader(policy, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("1".equals(required(values, "schema")), "unsupported gate latency schema");
        String mode = metrics.executed() == 0 ? "hot" : "cold";
        long limit = Long.parseLong(required(values, "slo." + mode + ".millis"));
        require(elapsedMillis < limit, mode + " gate latency SLO exceeded: "
                + elapsedMillis + "ms >= " + limit + "ms");
        validateTrend(root, values);
        System.out.println("  gate latency: " + mode + " " + elapsedMillis + "ms < " + limit + "ms");
        return mode;
    }

    private static void validateTrend(Path root, Properties values) throws IOException {
        int count = Integer.parseInt(required(values, "sample.count"));
        require(count >= 2, "gate latency trend needs cold and hot samples");
        String document = Files.readString(root.resolve("docs/generated/GATE_LATENCY.md"),
                StandardCharsets.UTF_8);
        for (int index = 1; index <= count; index++) {
            String prefix = "sample." + index + ".";
            String mode = required(values, prefix + "mode");
            long millis = Long.parseLong(required(values, prefix + "millis"));
            long limit = Long.parseLong(required(values, "slo." + mode + ".millis"));
            require(millis < limit, "versioned " + mode + " sample violates its SLO");
            require(document.contains("| " + required(values, prefix + "date") + " | " + mode
                    + " | " + millis + " |"), "gate latency document drift");
        }
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing gate latency field " + key);
        return value.trim();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
