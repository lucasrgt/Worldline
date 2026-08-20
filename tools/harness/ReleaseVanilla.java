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
        Properties m246 = load(root, "smokes/m246-spruce-log/smoke.properties");
        Properties m247 = load(root, "smokes/m247-birch-log/smoke.properties");
        Properties m248 = load(root, "smokes/m248-orange-wool/smoke.properties");
        Properties m249 = load(root, "smokes/m249-yellow-wool/smoke.properties");
        match(release, "version", "1.237.0");
        match(release, "milestone", "m249-yellow-wool");
        same(release, "m246.signature", m246, "expected.signature");
        same(release, "server.sha256", m246, "server.jar.sha256");
        same(release, "m247.signature", m247, "expected.signature");
        same(release, "server.sha256", m247, "server.jar.sha256");
        same(release, "m248.signature", m248, "expected.signature");
        same(release, "server.sha256", m248, "server.jar.sha256");
        same(release, "m249.signature", m249, "expected.signature");
        same(release, "server.sha256", m249, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M246_SPRUCE_LOG.md", "docs/M246_CYCLE.md",
                "smokes/m246-spruce-log/MAP.md", "docs/M247_BIRCH_LOG.md", "docs/M247_CYCLE.md",
                "smokes/m247-birch-log/MAP.md", "docs/M248_ORANGE_WOOL.md", "docs/M248_CYCLE.md",
                "smokes/m248-orange-wool/MAP.md", "docs/M249_YELLOW_WOOL.md", "docs/M249_CYCLE.md",
                "smokes/m249-yellow-wool/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.237.0 M249 Yellow wool GO");
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
