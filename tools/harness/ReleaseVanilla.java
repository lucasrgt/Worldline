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
        Properties m166 = load(root, "smokes/m166-note-block/smoke.properties");
        Properties m167 = load(root, "smokes/m167-cactus/smoke.properties");
        Properties m168 = load(root, "smokes/m168-water-bucket/smoke.properties");
        Properties m169 = load(root, "smokes/m169-egg-throw/smoke.properties");
        match(release, "version", "1.157.0");
        match(release, "milestone", "m169-egg-throw");
        same(release, "m166.signature", m166, "expected.signature");
        same(release, "server.sha256", m166, "server.jar.sha256");
        same(release, "m167.signature", m167, "expected.signature");
        same(release, "server.sha256", m167, "server.jar.sha256");
        same(release, "m168.signature", m168, "expected.signature");
        same(release, "server.sha256", m168, "server.jar.sha256");
        same(release, "m169.signature", m169, "expected.signature");
        same(release, "server.sha256", m169, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M166_NOTE_BLOCK.md", "docs/M166_CYCLE.md",
                "smokes/m166-note-block/MAP.md", "docs/M167_CACTUS.md", "docs/M167_CYCLE.md",
                "smokes/m167-cactus/MAP.md", "docs/M168_WATER_BUCKET.md", "docs/M168_CYCLE.md",
                "smokes/m168-water-bucket/MAP.md", "docs/M169_EGG_THROW.md", "docs/M169_CYCLE.md",
                "smokes/m169-egg-throw/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.157.0 M169 Egg-throw GO");
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
