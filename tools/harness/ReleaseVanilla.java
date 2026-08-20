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
        Properties m320 = load(root, "smokes/m320-leather-armor-crafts/smoke.properties");
        Properties m321 = load(root, "smokes/m321-gold-armor-crafts/smoke.properties");
        Properties m322 = load(root, "smokes/m322-diamond-armor-crafts/smoke.properties");
        Properties m323 = load(root, "smokes/m323-iron-tool-crafts/smoke.properties");
        match(release, "version", "1.311.0");
        match(release, "milestone", "m323-iron-tool-crafts");
        same(release, "m320.signature", m320, "expected.signature");
        same(release, "server.sha256", m320, "server.jar.sha256");
        same(release, "m321.signature", m321, "expected.signature");
        same(release, "server.sha256", m321, "server.jar.sha256");
        same(release, "m322.signature", m322, "expected.signature");
        same(release, "server.sha256", m322, "server.jar.sha256");
        same(release, "m323.signature", m323, "expected.signature");
        same(release, "server.sha256", m323, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M320_LEATHER_ARMOR_CRAFTS.md", "docs/M320_CYCLE.md",
                "smokes/m320-leather-armor-crafts/MAP.md", "docs/M321_GOLD_ARMOR_CRAFTS.md", "docs/M321_CYCLE.md",
                "smokes/m321-gold-armor-crafts/MAP.md", "docs/M322_DIAMOND_ARMOR_CRAFTS.md", "docs/M322_CYCLE.md",
                "smokes/m322-diamond-armor-crafts/MAP.md", "docs/M323_IRON_TOOL_CRAFTS.md", "docs/M323_CYCLE.md",
                "smokes/m323-iron-tool-crafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.311.0 M323 Iron tool crafts GO");
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
