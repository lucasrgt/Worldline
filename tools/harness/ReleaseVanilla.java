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
        Properties m303 = load(root, "smokes/m303-crop-harvests/smoke.properties");
        Properties m304 = load(root, "smokes/m304-farmland-set/smoke.properties");
        Properties m305 = load(root, "smokes/m305-plant-growth/smoke.properties");
        Properties m306 = load(root, "smokes/m306-closables/smoke.properties");
        match(release, "version", "1.294.0");
        match(release, "milestone", "m306-closables");
        same(release, "m303.signature", m303, "expected.signature");
        same(release, "server.sha256", m303, "server.jar.sha256");
        same(release, "m304.signature", m304, "expected.signature");
        same(release, "server.sha256", m304, "server.jar.sha256");
        same(release, "m305.signature", m305, "expected.signature");
        same(release, "server.sha256", m305, "server.jar.sha256");
        same(release, "m306.signature", m306, "expected.signature");
        same(release, "server.sha256", m306, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M303_CROP_HARVESTS.md", "docs/M303_CYCLE.md",
                "smokes/m303-crop-harvests/MAP.md", "docs/M304_FARMLAND_SET.md", "docs/M304_CYCLE.md",
                "smokes/m304-farmland-set/MAP.md", "docs/M305_PLANT_GROWTH.md", "docs/M305_CYCLE.md",
                "smokes/m305-plant-growth/MAP.md", "docs/M306_CLOSABLES.md", "docs/M306_CYCLE.md",
                "smokes/m306-closables/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.294.0 M306 Closables GO");
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
