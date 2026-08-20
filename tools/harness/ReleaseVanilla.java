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
        Properties m376 = load(root, "smokes/m376-remaining-shovel-breaks/smoke.properties");
        Properties m377 = load(root, "smokes/m377-powered-rail-motion/smoke.properties");
        Properties m378 = load(root, "smokes/m378-boat-water-set/smoke.properties");
        Properties m379 = load(root, "smokes/m379-iron-door-set/smoke.properties");
        match(release, "version", "1.367.0");
        match(release, "milestone", "m379-iron-door-set");
        same(release, "m376.signature", m376, "expected.signature");
        same(release, "server.sha256", m376, "server.jar.sha256");
        same(release, "m377.signature", m377, "expected.signature");
        same(release, "server.sha256", m377, "server.jar.sha256");
        same(release, "m378.signature", m378, "expected.signature");
        same(release, "server.sha256", m378, "server.jar.sha256");
        same(release, "m379.signature", m379, "expected.signature");
        same(release, "server.sha256", m379, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M376_REMAINING_SHOVEL_BREAKS.md", "docs/M376_CYCLE.md",
                "smokes/m376-remaining-shovel-breaks/MAP.md", "docs/M377_POWERED_RAIL_MOTION.md", "docs/M377_CYCLE.md",
                "smokes/m377-powered-rail-motion/MAP.md", "docs/M378_BOAT_WATER_SET.md", "docs/M378_CYCLE.md",
                "smokes/m378-boat-water-set/MAP.md", "docs/M379_IRON_DOOR_SET.md", "docs/M379_CYCLE.md",
                "smokes/m379-iron-door-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.367.0 M379 Iron door set GO");
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
