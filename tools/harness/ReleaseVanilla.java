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
        Properties m288 = load(root, "smokes/m288-brown-wool/smoke.properties");
        Properties m289 = load(root, "smokes/m289-spruce-sapling/smoke.properties");
        Properties m290 = load(root, "smokes/m290-birch-sapling/smoke.properties");
        Properties m291 = load(root, "smokes/m291-spruce-leaves/smoke.properties");
        match(release, "version", "1.279.0");
        match(release, "milestone", "m291-spruce-leaves");
        same(release, "m288.signature", m288, "expected.signature");
        same(release, "server.sha256", m288, "server.jar.sha256");
        same(release, "m289.signature", m289, "expected.signature");
        same(release, "server.sha256", m289, "server.jar.sha256");
        same(release, "m290.signature", m290, "expected.signature");
        same(release, "server.sha256", m290, "server.jar.sha256");
        same(release, "m291.signature", m291, "expected.signature");
        same(release, "server.sha256", m291, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M288_BROWN_WOOL.md", "docs/M288_CYCLE.md",
                "smokes/m288-brown-wool/MAP.md", "docs/M289_SPRUCE_SAPLING.md", "docs/M289_CYCLE.md",
                "smokes/m289-spruce-sapling/MAP.md", "docs/M290_BIRCH_SAPLING.md", "docs/M290_CYCLE.md",
                "smokes/m290-birch-sapling/MAP.md", "docs/M291_SPRUCE_LEAVES.md", "docs/M291_CYCLE.md",
                "smokes/m291-spruce-leaves/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.279.0 M291 Spruce leaves GO");
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
