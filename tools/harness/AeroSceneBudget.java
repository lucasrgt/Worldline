import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Enforces paired, machine-relative complete-frame budgets for Aero scenes. */
final class AeroSceneBudget {
    private static final List<String> METRICS = List.of("median", "p95", "p99", "max");
    private static final Pattern PAIR = Pattern.compile("(?m)^\\s*pair ([0-9]+): "
            + "absent:[^\\r\\n]*intervalNs=([0-9]+)/([0-9]+)/([0-9]+)/([0-9]+)[^\\r\\n]*"
            + "\\| present:[^\\r\\n]*intervalNs=([0-9]+)/([0-9]+)/([0-9]+)/([0-9]+)");

    private AeroSceneBudget() { }

    static void validateDescriptor(Path root, Properties descriptor) throws Exception {
        String scene = descriptor.getProperty("performance.scene", "").trim();
        if (scene.isEmpty()) return;
        require(scene.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid performance.scene");
        require("absent".equals(descriptor.getProperty("performance.baseline"))
                && "present".equals(descriptor.getProperty("performance.treatment")),
                "Aero scene budget requires absent/present paired arms");
        Path budget = budget(root, descriptor); Properties values = load(budget);
        require("1".equals(values.getProperty("schema")), "unsupported Aero scene budget schema");
        String prefix = "scene." + scene + ".";
        integer(values, prefix + "pairs", 1, 16);
        for (String metric : METRICS) limit(values, prefix, metric);
    }

    static void validateEvidence(Path root, Properties descriptor, String output) throws Exception {
        String scene = descriptor.getProperty("performance.scene", "").trim();
        if (scene.isEmpty()) return;
        Properties values = load(budget(root, descriptor)); String prefix = "scene." + scene + ".";
        int expectedPairs = integer(values, prefix + "pairs", 1, 16), observed = 0;
        List<String> violations = new ArrayList<>(); Matcher matcher = PAIR.matcher(output);
        while (matcher.find()) {
            observed++; require(Integer.parseInt(matcher.group(1)) == observed, "Aero pair order drifted");
            for (int index = 0; index < METRICS.size(); index++) {
                long baseline = Long.parseLong(matcher.group(2 + index));
                long treatment = Long.parseLong(matcher.group(6 + index));
                Limit limit = limit(values, prefix, METRICS.get(index));
                long maximum = scaled(baseline, limit.numerator, limit.denominator, limit.slack);
                if (treatment > maximum) violations.add("pair=" + observed + ",metric=" + METRICS.get(index)
                        + ",treatment=" + treatment + ",maximum=" + maximum);
            }
        }
        require(observed == expectedPairs, "Aero scene budget expected " + expectedPairs
                + " pairs but observed " + observed);
        require(violations.isEmpty(), "Aero scene budget violations: " + String.join(";", violations));
        System.out.println("  Aero scene budget: " + scene + " PASS across " + observed + " pairs");
    }

    private static long scaled(long baseline, int numerator, int denominator, long slack) {
        BigInteger product = BigInteger.valueOf(baseline).multiply(BigInteger.valueOf(numerator));
        BigInteger[] division = product.divideAndRemainder(BigInteger.valueOf(denominator));
        BigInteger ceiling = division[0].add(division[1].signum() == 0 ? BigInteger.ZERO : BigInteger.ONE);
        return ceiling.add(BigInteger.valueOf(slack)).min(BigInteger.valueOf(Long.MAX_VALUE)).longValueExact();
    }

    private static Limit limit(Properties values, String prefix, String metric) {
        String ratio = required(values, prefix + metric + ".ratio.max");
        require(ratio.matches("[1-9][0-9]?/[1-9][0-9]?"), "invalid Aero ratio: " + ratio);
        String[] fields = ratio.split("/");
        int numerator = Integer.parseInt(fields[0]), denominator = Integer.parseInt(fields[1]);
        require(numerator <= 64 && denominator <= 64, "Aero ratio exceeds 64");
        long slack = decimal(values, prefix + metric + ".slack.nanos");
        return new Limit(numerator, denominator, slack);
    }

    private static Path budget(Path root, Properties descriptor) {
        String value = descriptor.getProperty("performance.budget", "").trim();
        require(value.matches("quality/[a-z0-9-]+\\.properties"), "unsafe performance.budget");
        Path path = root.resolve(value).normalize();
        require(Files.isRegularFile(path), "missing " + value); return path;
    }
    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values;
    }
    private static int integer(Properties values, String key, int minimum, int maximum) {
        long value = decimal(values, key); require(value >= minimum && value <= maximum, "invalid " + key);
        return (int) value;
    }
    private static long decimal(Properties values, String key) {
        String value = required(values, key); require(value.matches("[0-9]{1,18}"), "invalid " + key);
        return Long.parseLong(value);
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key, "").trim(); require(!value.isEmpty(), "missing " + key); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Limit(int numerator, int denominator, long slack) { }
}
