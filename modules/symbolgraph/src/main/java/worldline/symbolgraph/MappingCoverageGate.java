package worldline.symbolgraph;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/** Exact fail-closed ratchet for a deterministic mapping coverage report. */
public final class MappingCoverageGate {
    private MappingCoverageGate() {}

    public static void verify(MappingCoverageReport report, Path policyPath) throws Exception {
        if (report == null || policyPath == null) throw new NullPointerException("mapping coverage gate input");
        Properties policy = new Properties();
        try (Reader reader = Files.newBufferedReader(policyPath, StandardCharsets.UTF_8)) {
            policy.load(reader);
        }
        require("1".equals(required(policy, "schema")), "unsupported mapping coverage policy schema");
        require(policy.size() == report.metrics().size() + 2,
                "mapping coverage policy does not enumerate every metric");
        for (Map.Entry<String, String> metric : report.metrics().entrySet()) {
            String key = "expected." + metric.getKey();
            require(metric.getValue().equals(required(policy, key)), "mapping coverage drift at " + key);
        }
        require(report.sha256().equals(required(policy, "expected.report.sha256")),
                "mapping coverage report digest drift");
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing mapping coverage property " + key);
        return value.trim();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
