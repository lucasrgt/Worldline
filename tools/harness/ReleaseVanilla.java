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
        Properties m292 = load(root, "smokes/m292-birch-leaves/smoke.properties");
        Properties m293 = load(root, "smokes/m293-sticky-piston-place/smoke.properties");
        Properties m294 = load(root, "smokes/m294-piston-place/smoke.properties");
        Properties m295 = load(root, "smokes/m295-pressure-plates/smoke.properties");
        match(release, "version", "1.283.0");
        match(release, "milestone", "m295-pressure-plates");
        same(release, "m292.signature", m292, "expected.signature");
        same(release, "server.sha256", m292, "server.jar.sha256");
        same(release, "m293.signature", m293, "expected.signature");
        same(release, "server.sha256", m293, "server.jar.sha256");
        same(release, "m294.signature", m294, "expected.signature");
        same(release, "server.sha256", m294, "server.jar.sha256");
        same(release, "m295.signature", m295, "expected.signature");
        same(release, "server.sha256", m295, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M292_BIRCH_LEAVES.md", "docs/M292_CYCLE.md",
                "smokes/m292-birch-leaves/MAP.md", "docs/M293_STICKY_PISTON_PLACE.md", "docs/M293_CYCLE.md",
                "smokes/m293-sticky-piston-place/MAP.md", "docs/M294_PISTON_PLACE.md", "docs/M294_CYCLE.md",
                "smokes/m294-piston-place/MAP.md", "docs/M295_PRESSURE_PLATES.md", "docs/M295_CYCLE.md",
                "smokes/m295-pressure-plates/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.283.0 M295 Pressure plates GO");
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
