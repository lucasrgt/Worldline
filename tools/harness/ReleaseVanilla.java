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
        Properties m323 = load(root, "smokes/m323-iron-tool-crafts/smoke.properties");
        Properties m324 = load(root, "smokes/m324-furnace-rest-smelts/smoke.properties");
        Properties m325 = load(root, "smokes/m325-navigation-crafts/smoke.properties");
        Properties m326 = load(root, "smokes/m326-vehicle-crafts/smoke.properties");
        match(release, "version", "1.314.0");
        match(release, "milestone", "m326-vehicle-crafts");
        same(release, "m323.signature", m323, "expected.signature");
        same(release, "server.sha256", m323, "server.jar.sha256");
        same(release, "m324.signature", m324, "expected.signature");
        same(release, "server.sha256", m324, "server.jar.sha256");
        same(release, "m325.signature", m325, "expected.signature");
        same(release, "server.sha256", m325, "server.jar.sha256");
        same(release, "m326.signature", m326, "expected.signature");
        same(release, "server.sha256", m326, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M323_IRON_TOOL_CRAFTS.md", "docs/M323_CYCLE.md",
                "smokes/m323-iron-tool-crafts/MAP.md", "docs/M324_FURNACE_REST_SMELTS.md", "docs/M324_CYCLE.md",
                "smokes/m324-furnace-rest-smelts/MAP.md", "docs/M325_NAVIGATION_CRAFTS.md", "docs/M325_CYCLE.md",
                "smokes/m325-navigation-crafts/MAP.md", "docs/M326_VEHICLE_CRAFTS.md", "docs/M326_CYCLE.md",
                "smokes/m326-vehicle-crafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.314.0 M326 Vehicle crafts GO");
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
