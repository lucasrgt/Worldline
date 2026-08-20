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
        Properties m433 = load(root, "smokes/m433-remaining-chest-orient-set/smoke.properties");
        Properties m434 = load(root, "smokes/m434-remaining-sponge-glass-ice/smoke.properties");
        Properties m441 = load(root, "smokes/m441-remaining-food-rest-set/smoke.properties");
        Properties m420 = load(root, "smokes/m420-wolf-tame-set/smoke.properties");
        match(release, "version", "1.411.0");
        match(release, "milestone", "m420-wolf-tame-set");
        same(release, "m433.signature", m433, "expected.signature");
        same(release, "server.sha256", m433, "server.jar.sha256");
        same(release, "m434.signature", m434, "expected.signature");
        same(release, "server.sha256", m434, "server.jar.sha256");
        same(release, "m441.signature", m441, "expected.signature");
        same(release, "server.sha256", m441, "server.jar.sha256");
        same(release, "m420.signature", m420, "expected.signature");
        same(release, "server.sha256", m420, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M433_REMAINING_CHEST_ORIENT_SET.md", "docs/M433_CYCLE.md",
                "smokes/m433-remaining-chest-orient-set/MAP.md", "docs/M434_REMAINING_SPONGE_GLASS_ICE.md", "docs/M434_CYCLE.md",
                "smokes/m434-remaining-sponge-glass-ice/MAP.md", "docs/M441_REMAINING_FOOD_REST_SET.md", "docs/M441_CYCLE.md",
                "smokes/m441-remaining-food-rest-set/MAP.md", "docs/M420_WOLF_TAME_SET.md", "docs/M420_CYCLE.md",
                "smokes/m420-wolf-tame-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.411.0 M420 Wolf tame set GO");
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
