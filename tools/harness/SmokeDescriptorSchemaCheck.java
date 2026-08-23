import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Validates the canonical behavior/TestKit identity and explicit runner descriptor schema. */
final class SmokeDescriptorSchemaCheck {
    private SmokeDescriptorSchemaCheck() { }
    static void execute(Path root) throws Exception {
        int legacy = 0, qualification = 0, narratives = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            Properties values = load(root.resolve("smokes").resolve(smoke.id).resolve("smoke.properties"));
            require("1".equals(values.getProperty("smoke.schema")), "invalid smoke schema: " + smoke.id);
            require(smoke.id.equals(values.getProperty("id")), "smoke identity drift: " + smoke.id);
            require(smoke.runner.equals(values.getProperty("runner.source")),
                    "runner declaration drift: " + smoke.id);
            require(hash(values, "expected.signature") && present(values, "expected.signal"),
                    "behavior identity incomplete: " + smoke.id);
            require((present(values, "testkit.contract") || present(values, "behavior"))
                            && present(values, "testkit.fixture")
                            && present(values, "testkit.actions") && present(values, "testkit.observations")
                            && present(values, "testkit.binding") && present(values, "testkit.evidence"),
                    "TestKit identity incomplete: " + smoke.id);
            String era = values.getProperty("smoke.era", "");
            if (era.equals("qualification-v1")) {
                qualification++; require("1".equals(values.getProperty("qualification.schema")),
                        "qualification era drift: " + smoke.id);
                require("1".equals(values.getProperty("narrative.schema")),
                        "missing generated narrative: " + smoke.id);
                MilestoneNarrative.validate(root, values); narratives++;
            } else { require(era.equals("legacy"), "unknown smoke era: " + smoke.id); legacy++; }
        }
        require(legacy == 489 && qualification == 37 && narratives == 37,
                "smoke descriptor schema census drift");
        System.out.println("  smoke schema: 526 descriptors; 37 generated narratives");
    }
    private static boolean present(Properties values, String key) {
        return !values.getProperty(key, "").trim().isEmpty();
    }
    private static boolean hash(Properties values, String key) {
        return values.getProperty(key, "").matches("[0-9a-f]{64}");
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
