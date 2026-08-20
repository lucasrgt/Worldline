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
        Properties m427 = load(root, "smokes/m427-remaining-piston-orient-set/smoke.properties");
        Properties m428 = load(root, "smokes/m428-remaining-door-orient-set/smoke.properties");
        Properties m429 = load(root, "smokes/m429-remaining-attach-faces/smoke.properties");
        Properties m430 = load(root, "smokes/m430-remaining-painting-motives/smoke.properties");
        match(release, "version", "1.420.0");
        match(release, "milestone", "m430-remaining-painting-motives");
        same(release, "m427.signature", m427, "expected.signature");
        same(release, "server.sha256", m427, "server.jar.sha256");
        same(release, "m428.signature", m428, "expected.signature");
        same(release, "server.sha256", m428, "server.jar.sha256");
        same(release, "m429.signature", m429, "expected.signature");
        same(release, "server.sha256", m429, "server.jar.sha256");
        same(release, "m430.signature", m430, "expected.signature");
        same(release, "server.sha256", m430, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M427_REMAINING_PISTON_ORIENT_SET.md", "docs/M427_CYCLE.md",
                "smokes/m427-remaining-piston-orient-set/MAP.md", "docs/M428_REMAINING_DOOR_ORIENT_SET.md", "docs/M428_CYCLE.md",
                "smokes/m428-remaining-door-orient-set/MAP.md", "docs/M429_REMAINING_ATTACH_FACES.md", "docs/M429_CYCLE.md",
                "smokes/m429-remaining-attach-faces/MAP.md", "docs/M430_REMAINING_PAINTING_MOTIVES.md", "docs/M430_CYCLE.md",
                "smokes/m430-remaining-painting-motives/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.420.0 M430 Remaining painting motives GO");
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
