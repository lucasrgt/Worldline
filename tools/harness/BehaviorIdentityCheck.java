import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Validates canonical behavior aliases, retractions, and their accepted runtime receipts. */
final class BehaviorIdentityCheck {
    private BehaviorIdentityCheck() { }

    static void execute(Path root) throws Exception {
        Properties decision = load(root.resolve("smokes/behavior-identity.lock"));
        Properties train = TrainPinCheck.manifest(root);
        require("1".equals(decision.getProperty("schema"))
                && "2".equals(decision.getProperty("alias.count"))
                && "1".equals(decision.getProperty("retracted.count")), "invalid behavior identity lock");
        Map<String, Integer> counts = behaviorCounts(root);
        for (int index = 0; index < 2; index++) {
            String stem = "alias." + index + ".";
            String legacy = required(decision, stem + "legacy");
            String canonical = required(decision, stem + "canonical");
            String smoke = required(decision, stem + "smoke");
            require(counts.getOrDefault(legacy, 0) == 0 && counts.getOrDefault(canonical, 0) == 1,
                    "behavior alias descriptor drift: " + legacy);
            require(canonical.equals(descriptor(root, smoke).getProperty("behavior")),
                    "behavior alias target drift: " + legacy);
            String trainStem = "smoke." + smoke + ".receipt.";
            for (String field : new String[] {"head", "tree", "signature"})
                require(required(decision, stem + field).equals(train.getProperty(trainStem + field)),
                        "behavior receipt drift: " + smoke + " " + field);
            require(required(decision, stem + "observation_sha256").equals(
                    train.getProperty("smoke." + smoke + ".evidence_sha256")),
                    "behavior observation drift: " + smoke);
        }
        String retired = required(decision, "retracted.0.token");
        require("tnt-quasi-connectivity".equals(retired) && counts.getOrDefault(retired, 0) == 0
                && required(decision, "retracted.0.reason").contains("M552"),
                "retracted behavior drift");
        System.out.println("  behavior identities: 2 aliases, 1 evidence-backed retraction");
    }

    private static Map<String, Integer> behaviorCounts(Path root) throws Exception {
        Map<String, Integer> counts = new HashMap<>();
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String token = descriptor(root, smoke.id).getProperty("behavior", "").trim();
            counts.merge(token, 1, Integer::sum);
        }
        return counts;
    }
    private static Properties descriptor(Path root, String id) throws Exception {
        return load(root.resolve("smokes").resolve(id).resolve("smoke.properties"));
    }
    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values;
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(), "missing " + key);
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
