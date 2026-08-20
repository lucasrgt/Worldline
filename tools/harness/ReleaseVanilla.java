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
        Properties m269 = load(root, "smokes/m269-shears-leaves/smoke.properties");
        Properties m270 = load(root, "smokes/m270-iron-helmet/smoke.properties");
        Properties m271 = load(root, "smokes/m271-gold-chestplate/smoke.properties");
        Properties m272 = load(root, "smokes/m272-diamond-leggings/smoke.properties");
        match(release, "version", "1.260.0");
        match(release, "milestone", "m272-diamond-leggings");
        same(release, "m269.signature", m269, "expected.signature");
        same(release, "server.sha256", m269, "server.jar.sha256");
        same(release, "m270.signature", m270, "expected.signature");
        same(release, "server.sha256", m270, "server.jar.sha256");
        same(release, "m271.signature", m271, "expected.signature");
        same(release, "server.sha256", m271, "server.jar.sha256");
        same(release, "m272.signature", m272, "expected.signature");
        same(release, "server.sha256", m272, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M269_SHEARS_LEAVES.md", "docs/M269_CYCLE.md",
                "smokes/m269-shears-leaves/MAP.md", "docs/M270_IRON_HELMET.md", "docs/M270_CYCLE.md",
                "smokes/m270-iron-helmet/MAP.md", "docs/M271_GOLD_CHESTPLATE.md", "docs/M271_CYCLE.md",
                "smokes/m271-gold-chestplate/MAP.md", "docs/M272_DIAMOND_LEGGINGS.md", "docs/M272_CYCLE.md",
                "smokes/m272-diamond-leggings/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.260.0 M272 Diamond leggings GO");
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
