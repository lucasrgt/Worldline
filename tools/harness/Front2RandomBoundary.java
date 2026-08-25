import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

/** Keeps fishing timing/loot and gravel flint-rate claims fail-closed. */
final class Front2RandomBoundary {
    private static final Set<String> KEYS = Set.of("schema", "status", "decision", "scope",
            "current.fishing", "current.gravel", "attempt.number", "probability.rate",
            "reopen.requires", "rate.reopen.requires", "document");
    private static final Set<String> FORBIDDEN_BEHAVIORS = Set.of(
            "fishing-bite-timing", "fishing-loot-depth", "gravel-flint-drop-rate");

    private Front2RandomBoundary() { }

    static void validate(Path root) throws Exception {
        Properties values = StrictProperties.load(
                root.resolve("quality/front2-random-boundary.properties"));
        require(values.stringPropertyNames().equals(KEYS), "FRONT2-08 boundary keys drifted");
        match(values, "schema", "1");
        match(values, "status", "registered");
        match(values, "decision", "explicit-non-claim-with-bounded-reopen");
        match(values, "scope", "fishing-bite-timing,fishing-loot-depth,gravel-flint-drop-rate");
        match(values, "current.fishing", "fishing-hook-and-event-triggered-raw-fish-only");
        match(values, "current.gravel", "placement-and-gravity-only");
        match(values, "attempt.number", "excluded");
        match(values, "probability.rate", "excluded");
        match(values, "reopen.requires", "official-two-replica-bounded-attempt-testkit-contract");
        match(values, "rate.reopen.requires", "reviewed-deterministic-draw-matrix");

        Path document = root.resolve(required(values, "document")).normalize();
        require(document.startsWith(root) && Files.isRegularFile(document),
                "missing FRONT2-08 decision document");
        String decision = Files.readString(document, StandardCharsets.UTF_8);
        for (String phrase : Set.of("explicit non-claim with a bounded-RNG reopening rule",
                "official Beta 1.7.3 oracle", "two fresh replicas", "BoundedAttempts.until",
                "exclude the successful attempt number", "reviewed deterministic draw matrix"))
            require(decision.contains(phrase), "FRONT2-08 decision lacks: " + phrase);

        requireText(root.resolve("docs/M360_FISHING_CATCH_SET.md"), "junk loot");
        requireText(root.resolve("smokes/m360-fishing-catch-set/MAP.md"),
                "Catch RNG is not hashed");
        requireText(root.resolve("docs/M218_GRAVEL.md"), "does not claim flint drops");
        Path gravelMap = root.resolve("smokes/m598-gravel-fall-set/MAP.md");
        requireText(gravelMap, "flint");
        requireText(gravelMap, "`318`");
        requireText(root.resolve("modules/testkit/src/main/java/worldline/testkit/BoundedAttempts.java"),
                "Result<T> until");

        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            Properties descriptor = StrictProperties.load(root.resolve("smokes").resolve(smoke.id)
                    .resolve("smoke.properties"));
            String behavior = descriptor.getProperty("behavior", "").trim();
            require(!FORBIDDEN_BEHAVIORS.contains(behavior),
                    behavior + " exists while FRONT2-08 remains a non-claim: " + smoke.id);
        }
        System.out.println("  FRONT2-08 random boundaries: explicit non-claims registered");
    }

    private static void requireText(Path path, String phrase) throws Exception {
        require(Files.readString(path, StandardCharsets.UTF_8).contains(phrase),
                path.getFileName() + " lacks boundary phrase: " + phrase);
    }

    private static void match(Properties values, String key, String expected) {
        require(expected.equals(required(values, key)), "FRONT2-08 field drift: " + key);
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key, "").trim();
        require(!value.isEmpty(), "missing FRONT2-08 field: " + key);
        return value;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
