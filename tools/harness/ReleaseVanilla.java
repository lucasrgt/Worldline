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
        Properties m294 = load(root, "smokes/m294-piston-place/smoke.properties");
        Properties m295 = load(root, "smokes/m295-pressure-plates/smoke.properties");
        Properties m296 = load(root, "smokes/m296-furnace-smelts/smoke.properties");
        Properties m297 = load(root, "smokes/m297-basic-crafts/smoke.properties");
        match(release, "version", "1.285.0");
        match(release, "milestone", "m297-basic-crafts");
        same(release, "m294.signature", m294, "expected.signature");
        same(release, "server.sha256", m294, "server.jar.sha256");
        same(release, "m295.signature", m295, "expected.signature");
        same(release, "server.sha256", m295, "server.jar.sha256");
        same(release, "m296.signature", m296, "expected.signature");
        same(release, "server.sha256", m296, "server.jar.sha256");
        same(release, "m297.signature", m297, "expected.signature");
        same(release, "server.sha256", m297, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M294_PISTON_PLACE.md", "docs/M294_CYCLE.md",
                "smokes/m294-piston-place/MAP.md", "docs/M295_PRESSURE_PLATES.md", "docs/M295_CYCLE.md",
                "smokes/m295-pressure-plates/MAP.md", "docs/M296_FURNACE_SMELTS.md", "docs/M296_CYCLE.md",
                "smokes/m296-furnace-smelts/MAP.md", "docs/M297_BASIC_CRAFTS.md", "docs/M297_CYCLE.md",
                "smokes/m297-basic-crafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.285.0 M297 Basic crafts GO");
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
