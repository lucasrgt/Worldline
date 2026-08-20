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
        Properties m337 = load(root, "smokes/m337-utility-item-crafts/smoke.properties");
        Properties m338 = load(root, "smokes/m338-furnace-fuel-set/smoke.properties");
        Properties m339 = load(root, "smokes/m339-sapling-growth-set/smoke.properties");
        Properties m340 = load(root, "smokes/m340-redstone-input-set/smoke.properties");
        match(release, "version", "1.328.0");
        match(release, "milestone", "m340-redstone-input-set");
        same(release, "m337.signature", m337, "expected.signature");
        same(release, "server.sha256", m337, "server.jar.sha256");
        same(release, "m338.signature", m338, "expected.signature");
        same(release, "server.sha256", m338, "server.jar.sha256");
        same(release, "m339.signature", m339, "expected.signature");
        same(release, "server.sha256", m339, "server.jar.sha256");
        same(release, "m340.signature", m340, "expected.signature");
        same(release, "server.sha256", m340, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M337_UTILITY_ITEM_CRAFTS.md", "docs/M337_CYCLE.md",
                "smokes/m337-utility-item-crafts/MAP.md", "docs/M338_FURNACE_FUEL_SET.md", "docs/M338_CYCLE.md",
                "smokes/m338-furnace-fuel-set/MAP.md", "docs/M339_SAPLING_GROWTH_SET.md", "docs/M339_CYCLE.md",
                "smokes/m339-sapling-growth-set/MAP.md", "docs/M340_REDSTONE_INPUT_SET.md", "docs/M340_CYCLE.md",
                "smokes/m340-redstone-input-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.328.0 M340 Redstone input set GO");
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
