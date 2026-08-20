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
        Properties m365 = load(root, "smokes/m365-compass-point-set/smoke.properties");
        Properties m366 = load(root, "smokes/m366-map-fill-set/smoke.properties");
        Properties m367 = load(root, "smokes/m367-piston-motion-set/smoke.properties");
        Properties m368 = load(root, "smokes/m368-more-dye-wool-crafts/smoke.properties");
        match(release, "version", "1.356.0");
        match(release, "milestone", "m368-more-dye-wool-crafts");
        same(release, "m365.signature", m365, "expected.signature");
        same(release, "server.sha256", m365, "server.jar.sha256");
        same(release, "m366.signature", m366, "expected.signature");
        same(release, "server.sha256", m366, "server.jar.sha256");
        same(release, "m367.signature", m367, "expected.signature");
        same(release, "server.sha256", m367, "server.jar.sha256");
        same(release, "m368.signature", m368, "expected.signature");
        same(release, "server.sha256", m368, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M365_COMPASS_POINT_SET.md", "docs/M365_CYCLE.md",
                "smokes/m365-compass-point-set/MAP.md", "docs/M366_MAP_FILL_SET.md", "docs/M366_CYCLE.md",
                "smokes/m366-map-fill-set/MAP.md", "docs/M367_PISTON_MOTION_SET.md", "docs/M367_CYCLE.md",
                "smokes/m367-piston-motion-set/MAP.md", "docs/M368_MORE_DYE_WOOL_CRAFTS.md", "docs/M368_CYCLE.md",
                "smokes/m368-more-dye-wool-crafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.356.0 M368 More dye wool crafts GO");
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
