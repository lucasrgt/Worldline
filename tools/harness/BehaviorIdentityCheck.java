import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Validates canonical behavior aliases, retractions, and their accepted runtime receipts. */
final class BehaviorIdentityCheck {
    private BehaviorIdentityCheck() { }

    static void execute(Path root) throws Exception {
        Properties decision = load(root.resolve("smokes/behavior-identity.lock"));
        Properties train = TrainPinCheck.manifest(root);
        require("1".equals(decision.getProperty("schema")), "invalid behavior identity lock");
        int aliases = integer(decision, "alias.count");
        int retractions = integer(decision, "retracted.count");
        Map<String, Integer> counts = behaviorCounts(root);
        for (int index = 0; index < aliases; index++) {
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
        Set<String> retired = new HashSet<>();
        for (int index = 0; index < retractions; index++) {
            String stem = "retracted." + index + ".";
            String token = required(decision, stem + "token");
            require(retired.add(token) && counts.getOrDefault(token, 0) == 0
                            && required(decision, stem + "reason").length() >= 24,
                    "retracted behavior drift: " + token);
            String evidence = decision.getProperty(stem + "evidence", "").trim();
            if (!evidence.isEmpty()) require(Files.isRegularFile(root.resolve(evidence))
                            && digest(root.resolve(evidence)).equals(
                                    required(decision, stem + "evidence_sha256")),
                    "retraction evidence drift: " + token);
        }
        System.out.println("  behavior identities: " + aliases + " aliases, " + retractions
                + " formal retractions");
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
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String digest(Path path) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
