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
        Properties values = new Properties(); String rebuild = System.getenv("WORLDLINE_CACHE_REBUILD_TOKEN");
        Path policy = root.resolve("quality/gate-latency.properties");
        try (Reader reader = Files.newBufferedReader(policy, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("1".equals(required(values, "schema")), "unsupported gate latency schema");
        String mode = rebuild == null || rebuild.isBlank()
                ? metrics.executed() == 0 ? "hot" : "cold" : "rebuild";
        long limit = mode.equals("rebuild") ? rebuildLimit(root, rebuild)
                : Long.parseLong(required(values, "slo." + mode + ".millis"));
        require(elapsedMillis < limit, mode + " gate latency SLO exceeded: "
                + elapsedMillis + "ms >= " + limit + "ms");
        validateTrend(root, values);
        System.out.println("  gate latency: " + mode + " " + elapsedMillis + "ms < " + limit + "ms");
        return mode;
    }

    private static long rebuildLimit(Path root, String token) throws IOException {
        String control = System.getenv("WORLDLINE_CONTROL_DIR");
        require(control != null && !control.isBlank(), "rebuild drill lacks isolated control");
        Path marker = Path.of(control).toAbsolutePath().normalize().resolve("cache-rebuild.marker");
        require(Files.isRegularFile(marker) && Files.readString(marker, StandardCharsets.UTF_8)
                .trim().equals(token), "rebuild drill token is not authorized");
        Properties policy = StrictProperties.load(root.resolve("quality/cache-rebuild-baseline.properties"));
        String platform = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "windows" : "linux";
        return Long.parseLong(required(policy, "maximum.seconds." + platform)) * 1_000L;
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
