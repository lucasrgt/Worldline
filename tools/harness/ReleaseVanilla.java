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
        Properties m275 = load(root, "smokes/m275-cactus-damage/smoke.properties");
        Properties m276 = load(root, "smokes/m276-fire-damage/smoke.properties");
        Properties m277 = load(root, "smokes/m277-wooden-door-open/smoke.properties");
        Properties m278 = load(root, "smokes/m278-trapdoor-toggle/smoke.properties");
        match(release, "version", "1.266.0");
        match(release, "milestone", "m278-trapdoor-toggle");
        same(release, "m275.signature", m275, "expected.signature");
        same(release, "server.sha256", m275, "server.jar.sha256");
        same(release, "m276.signature", m276, "expected.signature");
        same(release, "server.sha256", m276, "server.jar.sha256");
        same(release, "m277.signature", m277, "expected.signature");
        same(release, "server.sha256", m277, "server.jar.sha256");
        same(release, "m278.signature", m278, "expected.signature");
        same(release, "server.sha256", m278, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M275_CACTUS_DAMAGE.md", "docs/M275_CYCLE.md",
                "smokes/m275-cactus-damage/MAP.md", "docs/M276_FIRE_DAMAGE.md", "docs/M276_CYCLE.md",
                "smokes/m276-fire-damage/MAP.md", "docs/M277_WOODEN_DOOR_OPEN.md", "docs/M277_CYCLE.md",
                "smokes/m277-wooden-door-open/MAP.md", "docs/M278_TRAPDOOR_TOGGLE.md", "docs/M278_CYCLE.md",
                "smokes/m278-trapdoor-toggle/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.266.0 M278 Trapdoor toggle GO");
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
