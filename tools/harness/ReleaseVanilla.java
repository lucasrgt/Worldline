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
        Properties m202 = load(root, "smokes/m202-sapling/smoke.properties");
        Properties m203 = load(root, "smokes/m203-snow-layer/smoke.properties");
        Properties m204 = load(root, "smokes/m204-clay/smoke.properties");
        Properties m205 = load(root, "smokes/m205-brick/smoke.properties");
        match(release, "version", "1.193.0");
        match(release, "milestone", "m205-brick");
        same(release, "m202.signature", m202, "expected.signature");
        same(release, "server.sha256", m202, "server.jar.sha256");
        same(release, "m203.signature", m203, "expected.signature");
        same(release, "server.sha256", m203, "server.jar.sha256");
        same(release, "m204.signature", m204, "expected.signature");
        same(release, "server.sha256", m204, "server.jar.sha256");
        same(release, "m205.signature", m205, "expected.signature");
        same(release, "server.sha256", m205, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M202_SAPLING.md", "docs/M202_CYCLE.md",
                "smokes/m202-sapling/MAP.md", "docs/M203_SNOW_LAYER.md", "docs/M203_CYCLE.md",
                "smokes/m203-snow-layer/MAP.md", "docs/M204_CLAY.md", "docs/M204_CYCLE.md",
                "smokes/m204-clay/MAP.md", "docs/M205_BRICK.md", "docs/M205_CYCLE.md",
                "smokes/m205-brick/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.193.0 M205 Brick GO");
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
