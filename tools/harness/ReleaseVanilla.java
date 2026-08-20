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
        Properties m329 = load(root, "smokes/m329-utility-block-crafts/smoke.properties");
        Properties m330 = load(root, "smokes/m330-bed-sleep-set/smoke.properties");
        Properties m331 = load(root, "smokes/m331-throwables-set/smoke.properties");
        Properties m332 = load(root, "smokes/m332-bow-arrow-set/smoke.properties");
        match(release, "version", "1.320.0");
        match(release, "milestone", "m332-bow-arrow-set");
        same(release, "m329.signature", m329, "expected.signature");
        same(release, "server.sha256", m329, "server.jar.sha256");
        same(release, "m330.signature", m330, "expected.signature");
        same(release, "server.sha256", m330, "server.jar.sha256");
        same(release, "m331.signature", m331, "expected.signature");
        same(release, "server.sha256", m331, "server.jar.sha256");
        same(release, "m332.signature", m332, "expected.signature");
        same(release, "server.sha256", m332, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M329_UTILITY_BLOCK_CRAFTS.md", "docs/M329_CYCLE.md",
                "smokes/m329-utility-block-crafts/MAP.md", "docs/M330_BED_SLEEP_SET.md", "docs/M330_CYCLE.md",
                "smokes/m330-bed-sleep-set/MAP.md", "docs/M331_THROWABLES_SET.md", "docs/M331_CYCLE.md",
                "smokes/m331-throwables-set/MAP.md", "docs/M332_BOW_ARROW_SET.md", "docs/M332_CYCLE.md",
                "smokes/m332-bow-arrow-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.320.0 M332 Bow arrow set GO");
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
