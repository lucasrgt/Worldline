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
        Properties m243 = load(root, "smokes/m243-redstone-wire/smoke.properties");
        Properties m244 = load(root, "smokes/m244-cake-place/smoke.properties");
        Properties m245 = load(root, "smokes/m245-wall-sign/smoke.properties");
        Properties m246 = load(root, "smokes/m246-spruce-log/smoke.properties");
        match(release, "version", "1.234.0");
        match(release, "milestone", "m246-spruce-log");
        same(release, "m243.signature", m243, "expected.signature");
        same(release, "server.sha256", m243, "server.jar.sha256");
        same(release, "m244.signature", m244, "expected.signature");
        same(release, "server.sha256", m244, "server.jar.sha256");
        same(release, "m245.signature", m245, "expected.signature");
        same(release, "server.sha256", m245, "server.jar.sha256");
        same(release, "m246.signature", m246, "expected.signature");
        same(release, "server.sha256", m246, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M243_REDSTONE_WIRE.md", "docs/M243_CYCLE.md",
                "smokes/m243-redstone-wire/MAP.md", "docs/M244_CAKE_PLACE.md", "docs/M244_CYCLE.md",
                "smokes/m244-cake-place/MAP.md", "docs/M245_WALL_SIGN.md", "docs/M245_CYCLE.md",
                "smokes/m245-wall-sign/MAP.md", "docs/M246_SPRUCE_LOG.md", "docs/M246_CYCLE.md",
                "smokes/m246-spruce-log/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.234.0 M246 Spruce log GO");
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
