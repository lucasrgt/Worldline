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
        Properties m378 = load(root, "smokes/m378-boat-water-set/smoke.properties");
        Properties m379 = load(root, "smokes/m379-iron-door-set/smoke.properties");
        Properties m380 = load(root, "smokes/m380-trapdoor-family-set/smoke.properties");
        Properties m381 = load(root, "smokes/m381-tnt-prime-set/smoke.properties");
        match(release, "version", "1.369.0");
        match(release, "milestone", "m381-tnt-prime-set");
        same(release, "m378.signature", m378, "expected.signature");
        same(release, "server.sha256", m378, "server.jar.sha256");
        same(release, "m379.signature", m379, "expected.signature");
        same(release, "server.sha256", m379, "server.jar.sha256");
        same(release, "m380.signature", m380, "expected.signature");
        same(release, "server.sha256", m380, "server.jar.sha256");
        same(release, "m381.signature", m381, "expected.signature");
        same(release, "server.sha256", m381, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M378_BOAT_WATER_SET.md", "docs/M378_CYCLE.md",
                "smokes/m378-boat-water-set/MAP.md", "docs/M379_IRON_DOOR_SET.md", "docs/M379_CYCLE.md",
                "smokes/m379-iron-door-set/MAP.md", "docs/M380_TRAPDOOR_FAMILY_SET.md", "docs/M380_CYCLE.md",
                "smokes/m380-trapdoor-family-set/MAP.md", "docs/M381_TNT_PRIME_SET.md", "docs/M381_CYCLE.md",
                "smokes/m381-tnt-prime-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.369.0 M381 Tnt prime set GO");
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
