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
        Properties m423 = load(root, "smokes/m423-slimeball-set/smoke.properties");
        Properties m424 = load(root, "smokes/m424-furnace-cart-motion-set/smoke.properties");
        Properties m425 = load(root, "smokes/m425-remaining-machine-faces/smoke.properties");
        Properties m426 = load(root, "smokes/m426-remaining-redstone-faces/smoke.properties");
        match(release, "version", "1.416.0");
        match(release, "milestone", "m426-remaining-redstone-faces");
        same(release, "m423.signature", m423, "expected.signature");
        same(release, "server.sha256", m423, "server.jar.sha256");
        same(release, "m424.signature", m424, "expected.signature");
        same(release, "server.sha256", m424, "server.jar.sha256");
        same(release, "m425.signature", m425, "expected.signature");
        same(release, "server.sha256", m425, "server.jar.sha256");
        same(release, "m426.signature", m426, "expected.signature");
        same(release, "server.sha256", m426, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M423_SLIMEBALL_SET.md", "docs/M423_CYCLE.md",
                "smokes/m423-slimeball-set/MAP.md", "docs/M424_FURNACE_CART_MOTION_SET.md", "docs/M424_CYCLE.md",
                "smokes/m424-furnace-cart-motion-set/MAP.md", "docs/M425_REMAINING_MACHINE_FACES.md", "docs/M425_CYCLE.md",
                "smokes/m425-remaining-machine-faces/MAP.md", "docs/M426_REMAINING_REDSTONE_FACES.md", "docs/M426_CYCLE.md",
                "smokes/m426-remaining-redstone-faces/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.416.0 M426 Remaining redstone faces GO");
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
