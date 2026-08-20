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
        Properties m343 = load(root, "smokes/m343-fire-family-set/smoke.properties");
        Properties m344 = load(root, "smokes/m344-bucket-fluid-set/smoke.properties");
        Properties m345 = load(root, "smokes/m345-ore-block-crafts/smoke.properties");
        Properties m346 = load(root, "smokes/m346-ore-block-uncrafts/smoke.properties");
        match(release, "version", "1.334.0");
        match(release, "milestone", "m346-ore-block-uncrafts");
        same(release, "m343.signature", m343, "expected.signature");
        same(release, "server.sha256", m343, "server.jar.sha256");
        same(release, "m344.signature", m344, "expected.signature");
        same(release, "server.sha256", m344, "server.jar.sha256");
        same(release, "m345.signature", m345, "expected.signature");
        same(release, "server.sha256", m345, "server.jar.sha256");
        same(release, "m346.signature", m346, "expected.signature");
        same(release, "server.sha256", m346, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M343_FIRE_FAMILY_SET.md", "docs/M343_CYCLE.md",
                "smokes/m343-fire-family-set/MAP.md", "docs/M344_BUCKET_FLUID_SET.md", "docs/M344_CYCLE.md",
                "smokes/m344-bucket-fluid-set/MAP.md", "docs/M345_ORE_BLOCK_CRAFTS.md", "docs/M345_CYCLE.md",
                "smokes/m345-ore-block-crafts/MAP.md", "docs/M346_ORE_BLOCK_UNCRAFTS.md", "docs/M346_CYCLE.md",
                "smokes/m346-ore-block-uncrafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.334.0 M346 Ore block uncrafts GO");
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
