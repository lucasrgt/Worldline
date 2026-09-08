import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

/** Binds finite fishing and gravel claims to the official native random-outcome proof. */
final class Front2RandomBoundary {
    private static final Set<String> KEYS = Set.of("schema", "status", "decision", "scope",
            "current.fishing", "current.gravel", "attempt.number", "probability.rate",
            "reopen.requires", "rate.reopen.requires", "qualified.milestone", "document");
    private static final Set<String> FORBIDDEN_BEHAVIORS = Set.of(
            "fishing-bite-timing", "fishing-loot-depth", "gravel-flint-drop-rate");

    private Front2RandomBoundary() { }

    static void validate(Path root) throws Exception {
        Properties values = StrictProperties.load(
                root.resolve("quality/front2-random-boundary.properties"));
        require(values.stringPropertyNames().equals(KEYS), "FRONT2-08 boundary keys drifted");
        match(values, "schema", "2");
        match(values, "status", "registered");
        match(values, "decision", "bounded-official-claim");
        match(values, "scope", "fishing-bite-timing,fishing-loot-depth,gravel-flint-drop-rate");
        match(values, "current.fishing", "finite-weather-depth-draws-and-catch-windows");
        match(values, "current.gravel", "all-ten-drop-residues");
        match(values, "attempt.number", "controlled-first-bite-schedules-only");
        match(values, "probability.rate", "conditional-uniform-integer-residues-only");
        match(values, "qualified.milestone", "b173-native-random-outcomes-cycle");
        match(values, "reopen.requires", "official-two-replica-bounded-attempt-testkit-contract");
        match(values, "rate.reopen.requires", "reviewed-deterministic-draw-matrix");

        Path document = root.resolve(required(values, "document")).normalize();
        require(document.startsWith(root) && Files.isRegularFile(document),
                "missing FRONT2-08 decision document");
        String decision = Files.readString(document, StandardCharsets.UTF_8);
        for (String phrase : Set.of("bounded official claim",
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
                    behavior + " is outside the reviewed finite FRONT2-08 package: " + smoke.id);
        }
        Properties descriptor = StrictProperties.load(root.resolve("smokes")
                .resolve(required(values, "qualified.milestone")).resolve("smoke.properties"));
        descriptor(descriptor);
        Properties changed = new Properties(); changed.putAll(descriptor);
        changed.setProperty("testkit.binding", "net.minecraft.src.EntityFish#onUpdate");
        boolean rejected = false;
        try { descriptor(changed); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, "private native fishing binding accepted as public evidence");
        System.out.println("  FRONT2-08 random boundaries: bounded official draw matrix qualified");
    }

    private static void descriptor(Properties values) {
        match(values, "behavior", "native-random-outcomes");
        match(values, "native.capture.schema", "1");
        match(values, "runner.source", "tools/smoke/NativeInventoryRenderCycle.java");
        match(values, "testkit.binding", "worldline.testkit.RandomOutcomeFixture#verify");
        match(values, "testkit.evidence", "equatable");
        match(values, "milestone.atoms",
                "gravel-finite-drop-rate,fishing-weather-depth-matrix,catch-window-and-first-bite-timing");
        match(values, "expected.signal", "family=native-random-outcomes,subjects=2,observations=3228,"
                + "processes=4,oracle=mapped-official-native-state");
        require(required(values, "expected.signature").matches("[0-9a-f]{64}")
                && !required(values, "expected.signature").equals("0".repeat(64)), "unfrozen random proof");
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
