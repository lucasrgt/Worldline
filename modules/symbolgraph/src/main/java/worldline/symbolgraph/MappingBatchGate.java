package worldline.symbolgraph;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/** Exact fail-closed ratchet for one cumulative mapping qualification batch. */
public final class MappingBatchGate {
    private MappingBatchGate() {}

    public static void verify(MappingBatchReport report, Path policyPath) throws Exception {
        if (report == null || policyPath == null) throw new NullPointerException("mapping batch gate input");
        Properties policy = load(policyPath);
        require("1".equals(required(policy, "schema")), "unsupported mapping batch policy schema");
        require(policy.size() == report.metrics().size() + 2,
                "mapping batch policy does not enumerate every metric");
        for (Map.Entry<String, String> metric : report.metrics().entrySet()) {
            String key = "expected." + metric.getKey();
            String expected = required(policy, key);
            require(metric.getValue().equals(expected), "mapping batch drift at " + key
                    + ": expected " + expected + ", actual " + metric.getValue());
        }
        require(report.sha256().equals(required(policy, "expected.report.sha256")),
                "mapping batch report digest drift");
        require(report.metric("selected.total").equals(report.metric("selected.qualified")),
                "mapping batch selected an unqualified identity");
    }

    public static String policy(MappingBatchReport report) {
        if (report == null) throw new NullPointerException("mapping batch report");
        StringBuilder text = new StringBuilder("schema=1\n");
        for (Map.Entry<String, String> metric : report.metrics().entrySet())
            text.append("expected.").append(metric.getKey()).append('=').append(metric.getValue()).append('\n');
        return text.append("expected.report.sha256=").append(report.sha256()).append('\n').toString();
    }

    public static void verifyRetractions(MappingBatchReport report, Path policyPath) throws Exception {
        Properties policy = load(policyPath); int count = report.excludedIds().size();
        require("1".equals(required(policy, "schema")), "unsupported mapping retraction schema");
        require("nostalgia-only".equals(required(policy, "scope")), "unsupported retraction scope");
        require(report.sha256().equals(required(policy, "report.sha256")), "retraction report drift");
        require(Integer.toString(count).equals(required(policy, "retracted.count")),
                "retraction count drift");
        require("sem-m13-excluded-section".equals(required(policy, "source")),
                "unsupported retraction source");
        require(policy.size() == count + 5, "retraction policy must enumerate every identity");
        for (int index = 0; index < count; index++)
            require(report.excludedIds().get(index).equals(required(policy,
                    "retracted." + index + ".sha256")), "retracted identity drift at " + index);
    }

    public static String retractionPolicy(MappingBatchReport report) {
        StringBuilder text = new StringBuilder("schema=1\nscope=nostalgia-only\nreport.sha256=")
                .append(report.sha256()).append("\nretracted.count=")
                .append(report.excludedIds().size()).append("\nsource=sem-m13-excluded-section\n");
        for (int index = 0; index < report.excludedIds().size(); index++)
            text.append("retracted.").append(index).append(".sha256=")
                    .append(report.excludedIds().get(index)).append('\n');
        return text.toString();
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing mapping batch property " + key);
        return value.trim();
    }
    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
