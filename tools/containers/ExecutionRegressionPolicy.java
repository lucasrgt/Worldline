import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

/** Reviewed limits for non-authoritative executor performance signals. */
record ExecutionRegressionPolicy(String lane, String runtimeCapability, String termination,
        String metric, int baselineWindow, int recentWindow, int thresholdPercent) {
    private static final Set<String> KEYS = Set.of("schema", "scope.lane",
            "scope.runtime.capability", "scope.termination", "metric",
            "baseline.window.samples", "recent.window.samples", "change.threshold.percent",
            "authority", "admission.effect");

    static ExecutionRegressionPolicy load(Path repository) throws Exception {
        Path path = repository.resolve("tools/containers/execution-regression.properties");
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(path) && Files.size(path) <= 2_048L,
                "invalid execution regression policy file");
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require(values.stringPropertyNames().equals(KEYS), "execution regression policy fields drifted");
        require("worldline.execution-regression-policy.v1".equals(values.getProperty("schema"))
                && "observational-only".equals(values.getProperty("authority"))
                && "none".equals(values.getProperty("admission.effect")),
                "execution regression policy gained authority");
        String lane = token(values, "scope.lane"), capability = token(values,
                "scope.runtime.capability"), termination = token(values, "scope.termination");
        require("elapsed_ms".equals(values.getProperty("metric")), "unsupported regression metric");
        int baseline = number(values, "baseline.window.samples");
        int recent = number(values, "recent.window.samples");
        int threshold = number(values, "change.threshold.percent");
        require(baseline >= 3 && baseline <= 31 && (baseline & 1) == 1,
                "baseline window must be odd and between 3 and 31");
        require(recent >= 3 && recent <= 31 && (recent & 1) == 1,
                "recent window must be odd and between 3 and 31");
        require(threshold >= 110 && threshold <= 1_000,
                "change threshold must be between 110 and 1000 percent");
        return new ExecutionRegressionPolicy(lane, capability, termination, "elapsed_ms",
                baseline, recent, threshold);
    }

    boolean accepts(ExecutionTelemetrySample sample) {
        return lane.equals(sample.lane()) && runtimeCapability.equals(sample.runtimeCapability())
                && termination.equals(sample.telemetry().termination());
    }

    private static String token(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && value.matches("[a-z0-9][a-z0-9._-]{0,62}"),
                "invalid execution regression policy token: " + key);
        return value;
    }
    private static int number(Properties values, String key) {
        try { return Integer.parseInt(values.getProperty(key)); }
        catch (RuntimeException error) {
            throw new IllegalArgumentException(key + " is not an integer", error);
        }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
