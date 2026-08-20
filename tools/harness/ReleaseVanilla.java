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
        Properties m359 = load(root, "smokes/m359-bed-nether-explode/smoke.properties");
        Properties m360 = load(root, "smokes/m360-fishing-catch-set/smoke.properties");
        Properties m361 = load(root, "smokes/m361-ladder-climb-set/smoke.properties");
        Properties m362 = load(root, "smokes/m362-fence-collision-set/smoke.properties");
        match(release, "version", "1.350.0");
        match(release, "milestone", "m362-fence-collision-set");
        same(release, "m359.signature", m359, "expected.signature");
        same(release, "server.sha256", m359, "server.jar.sha256");
        same(release, "m360.signature", m360, "expected.signature");
        same(release, "server.sha256", m360, "server.jar.sha256");
        same(release, "m361.signature", m361, "expected.signature");
        same(release, "server.sha256", m361, "server.jar.sha256");
        same(release, "m362.signature", m362, "expected.signature");
        same(release, "server.sha256", m362, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M359_BED_NETHER_EXPLODE.md", "docs/M359_CYCLE.md",
                "smokes/m359-bed-nether-explode/MAP.md", "docs/M360_FISHING_CATCH_SET.md", "docs/M360_CYCLE.md",
                "smokes/m360-fishing-catch-set/MAP.md", "docs/M361_LADDER_CLIMB_SET.md", "docs/M361_CYCLE.md",
                "smokes/m361-ladder-climb-set/MAP.md", "docs/M362_FENCE_COLLISION_SET.md", "docs/M362_CYCLE.md",
                "smokes/m362-fence-collision-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.350.0 M362 Fence collision set GO");
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
