import java.util.Locale;
import java.util.Properties;

/** Reports applicable coverage while retaining candidate and exclusion counts separately. */
final class FunctionalCensusCoverage {
    private FunctionalCensusCoverage() { }
    static void selfTest() throws Exception {
        Properties schema = new Properties();
        schema.setProperty("target.proof.percent", "98.8");
        schema.setProperty("target.public-testkit.percent", "98.8");
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        java.io.PrintStream prior = System.out;
        try (java.io.PrintStream capture = new java.io.PrintStream(output, true, "UTF-8")) {
            System.setOut(capture);
            print("entity", 264, 142, 142, 10, schema);
        } finally { System.setOut(prior); }
        String report = output.toString("UTF-8");
        if (!report.contains("applicable=254, not-applicable=10")
                || !report.contains("proof=55.91%") || !report.contains("public-testkit=142/254")
                || !report.contains("verified-claims-to-target=109")) {
            throw new IllegalStateException("census report excluded unknowns or counted exclusions as proof");
        }
    }
    static void print(String id, int candidates, int verified, int publicTestkit,
            int excluded, Properties schema) {
        int applicable = candidates - excluded;
        double target = Double.parseDouble(required(schema, "target.proof.percent"));
        double publicTarget = Double.parseDouble(required(schema, "target.public-testkit.percent"));
        int targetClaims = (int) Math.ceil(applicable * target / 100.0);
        int publicClaims = (int) Math.ceil(applicable * publicTarget / 100.0);
        System.out.println("    applicable=" + applicable + ", not-applicable=" + excluded);
        System.out.println("    " + id + " proof=" + decimal(percent(verified, applicable))
                + "%, target=" + decimal(target) + "%, verified-claims-to-target="
                + Math.max(0, targetClaims - verified));
        System.out.println("    " + id + " public-testkit=" + publicTestkit + "/" + applicable
                + ", coverage=" + decimal(percent(publicTestkit, applicable)) + "%, target="
                + decimal(publicTarget) + "%, claims-to-target="
                + Math.max(0, publicClaims - publicTestkit));
    }

    private static double percent(int numerator, int denominator) {
        return denominator == 0 ? 0 : numerator * 100.0 / denominator;
    }
    private static String decimal(double value) { return String.format(Locale.ROOT, "%.2f", value); }

    private static String required(Properties schema, String key) {
        String value = schema.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing " + key);
        return value;
    }
}
