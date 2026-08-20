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
        Properties m430 = load(root, "smokes/m430-remaining-painting-motives/smoke.properties");
        Properties m431 = load(root, "smokes/m431-remaining-bed-orient-set/smoke.properties");
        Properties m432 = load(root, "smokes/m432-remaining-rail-geometry-set/smoke.properties");
        Properties m435 = load(root, "smokes/m435-remaining-natural-spawns/smoke.properties");
        match(release, "version", "1.423.0");
        match(release, "milestone", "m435-remaining-natural-spawns");
        same(release, "m430.signature", m430, "expected.signature");
        same(release, "server.sha256", m430, "server.jar.sha256");
        same(release, "m431.signature", m431, "expected.signature");
        same(release, "server.sha256", m431, "server.jar.sha256");
        same(release, "m432.signature", m432, "expected.signature");
        same(release, "server.sha256", m432, "server.jar.sha256");
        same(release, "m435.signature", m435, "expected.signature");
        same(release, "server.sha256", m435, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M430_REMAINING_PAINTING_MOTIVES.md", "docs/M430_CYCLE.md",
                "smokes/m430-remaining-painting-motives/MAP.md", "docs/M431_REMAINING_BED_ORIENT_SET.md", "docs/M431_CYCLE.md",
                "smokes/m431-remaining-bed-orient-set/MAP.md", "docs/M432_REMAINING_RAIL_GEOMETRY_SET.md", "docs/M432_CYCLE.md",
                "smokes/m432-remaining-rail-geometry-set/MAP.md", "docs/M435_REMAINING_NATURAL_SPAWNS.md", "docs/M435_CYCLE.md",
                "smokes/m435-remaining-natural-spawns/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.423.0 M435 Remaining natural spawns GO");
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
