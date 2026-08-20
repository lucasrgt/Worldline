import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

/** Latest vanilla milestone release gate, extracted from ReleaseCheck's packed file. */
final class ReleaseVanilla {
    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/ReleaseVanilla.java");
            System.exit(2);
        }
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            Properties release = load(root, "release/worldline.properties");
            check(root, release);
        } catch (Exception error) {
            System.err.println("release vanilla failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private ReleaseVanilla() {}

    static void check(Path root, Properties release) throws Exception {
        Properties m389 = load(root, "smokes/m389-animal-drops-set/smoke.properties");
        Properties m390 = load(root, "smokes/m390-remaining-spawner-set/smoke.properties");
        Properties m391 = load(root, "smokes/m391-creeper-explode-set/smoke.properties");
        Properties m392 = load(root, "smokes/m392-remaining-fluid-flow/smoke.properties");
        match(release, "version", "1.380.0");
        match(release, "milestone", "m392-remaining-fluid-flow");
        same(release, "m389.signature", m389, "expected.signature");
        same(release, "server.sha256", m389, "server.jar.sha256");
        same(release, "m390.signature", m390, "expected.signature");
        same(release, "server.sha256", m390, "server.jar.sha256");
        same(release, "m391.signature", m391, "expected.signature");
        same(release, "server.sha256", m391, "server.jar.sha256");
        same(release, "m392.signature", m392, "expected.signature");
        same(release, "server.sha256", m392, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M389_ANIMAL_DROPS_SET.md", "docs/M389_CYCLE.md",
                "smokes/m389-animal-drops-set/MAP.md", "docs/M390_REMAINING_SPAWNER_SET.md", "docs/M390_CYCLE.md",
                "smokes/m390-remaining-spawner-set/MAP.md", "docs/M391_CREEPER_EXPLODE_SET.md", "docs/M391_CYCLE.md",
                "smokes/m391-creeper-explode-set/MAP.md", "docs/M392_REMAINING_FLUID_FLOW.md", "docs/M392_CYCLE.md",
                "smokes/m392-remaining-fluid-flow/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.380.0 M392 Remaining fluid flow GO");
    }

    private static Properties load(Path root, String relative) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(root.resolve(relative), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static void match(Properties properties, String key, String expected) {
        String value = properties.getProperty(key);
        if (value == null || !expected.equals(value.trim()))
            throw new IllegalStateException("expected " + key + "=" + expected + " but was " + value);
    }

    private static void same(Properties left, String leftKey, Properties right, String rightKey) {
        String leftValue = left.getProperty(leftKey), rightValue = right.getProperty(rightKey);
        if (leftValue == null || rightValue == null || !leftValue.trim().equals(rightValue.trim()))
            throw new IllegalStateException(leftKey + " drifted from " + rightKey);
    }
}
