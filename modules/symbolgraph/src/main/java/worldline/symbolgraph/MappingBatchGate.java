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
        Properties policy = new Properties();
        try (Reader reader = Files.newBufferedReader(policyPath, StandardCharsets.UTF_8)) {
            policy.load(reader);
        }
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

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing mapping batch property " + key);
        return value.trim();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
