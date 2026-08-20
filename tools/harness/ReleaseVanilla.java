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
        Properties m325 = load(root, "smokes/m325-navigation-crafts/smoke.properties");
        Properties m326 = load(root, "smokes/m326-vehicle-crafts/smoke.properties");
        Properties m327 = load(root, "smokes/m327-food-crafts/smoke.properties");
        Properties m328 = load(root, "smokes/m328-dye-family-crafts/smoke.properties");
        match(release, "version", "1.316.0");
        match(release, "milestone", "m328-dye-family-crafts");
        same(release, "m325.signature", m325, "expected.signature");
        same(release, "server.sha256", m325, "server.jar.sha256");
        same(release, "m326.signature", m326, "expected.signature");
        same(release, "server.sha256", m326, "server.jar.sha256");
        same(release, "m327.signature", m327, "expected.signature");
        same(release, "server.sha256", m327, "server.jar.sha256");
        same(release, "m328.signature", m328, "expected.signature");
        same(release, "server.sha256", m328, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M325_NAVIGATION_CRAFTS.md", "docs/M325_CYCLE.md",
                "smokes/m325-navigation-crafts/MAP.md", "docs/M326_VEHICLE_CRAFTS.md", "docs/M326_CYCLE.md",
                "smokes/m326-vehicle-crafts/MAP.md", "docs/M327_FOOD_CRAFTS.md", "docs/M327_CYCLE.md",
                "smokes/m327-food-crafts/MAP.md", "docs/M328_DYE_FAMILY_CRAFTS.md", "docs/M328_CYCLE.md",
                "smokes/m328-dye-family-crafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.316.0 M328 Dye family crafts GO");
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
