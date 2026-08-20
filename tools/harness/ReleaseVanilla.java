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
        Properties m340 = load(root, "smokes/m340-redstone-input-set/smoke.properties");
        Properties m341 = load(root, "smokes/m341-repeater-delay-set/smoke.properties");
        Properties m342 = load(root, "smokes/m342-gravity-block-set/smoke.properties");
        Properties m343 = load(root, "smokes/m343-fire-family-set/smoke.properties");
        match(release, "version", "1.331.0");
        match(release, "milestone", "m343-fire-family-set");
        same(release, "m340.signature", m340, "expected.signature");
        same(release, "server.sha256", m340, "server.jar.sha256");
        same(release, "m341.signature", m341, "expected.signature");
        same(release, "server.sha256", m341, "server.jar.sha256");
        same(release, "m342.signature", m342, "expected.signature");
        same(release, "server.sha256", m342, "server.jar.sha256");
        same(release, "m343.signature", m343, "expected.signature");
        same(release, "server.sha256", m343, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M340_REDSTONE_INPUT_SET.md", "docs/M340_CYCLE.md",
                "smokes/m340-redstone-input-set/MAP.md", "docs/M341_REPEATER_DELAY_SET.md", "docs/M341_CYCLE.md",
                "smokes/m341-repeater-delay-set/MAP.md", "docs/M342_GRAVITY_BLOCK_SET.md", "docs/M342_CYCLE.md",
                "smokes/m342-gravity-block-set/MAP.md", "docs/M343_FIRE_FAMILY_SET.md", "docs/M343_CYCLE.md",
                "smokes/m343-fire-family-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.331.0 M343 Fire family set GO");
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
