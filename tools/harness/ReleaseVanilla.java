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
        Properties m240 = load(root, "smokes/m240-bed-place/smoke.properties");
        Properties m241 = load(root, "smokes/m241-iron-door-place/smoke.properties");
        Properties m242 = load(root, "smokes/m242-lever-place/smoke.properties");
        Properties m243 = load(root, "smokes/m243-redstone-wire/smoke.properties");
        match(release, "version", "1.231.0");
        match(release, "milestone", "m243-redstone-wire");
        same(release, "m240.signature", m240, "expected.signature");
        same(release, "server.sha256", m240, "server.jar.sha256");
        same(release, "m241.signature", m241, "expected.signature");
        same(release, "server.sha256", m241, "server.jar.sha256");
        same(release, "m242.signature", m242, "expected.signature");
        same(release, "server.sha256", m242, "server.jar.sha256");
        same(release, "m243.signature", m243, "expected.signature");
        same(release, "server.sha256", m243, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M240_BED_PLACE.md", "docs/M240_CYCLE.md",
                "smokes/m240-bed-place/MAP.md", "docs/M241_IRON_DOOR_PLACE.md", "docs/M241_CYCLE.md",
                "smokes/m241-iron-door-place/MAP.md", "docs/M242_LEVER_PLACE.md", "docs/M242_CYCLE.md",
                "smokes/m242-lever-place/MAP.md", "docs/M243_REDSTONE_WIRE.md", "docs/M243_CYCLE.md",
                "smokes/m243-redstone-wire/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.231.0 M243 Redstone wire GO");
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
