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
        Properties m453 = load(root, "smokes/m453-player-death-drops-set/smoke.properties");
        Properties m454 = load(root, "smokes/m454-peaceful-despawn-set/smoke.properties");
        Properties m455 = load(root, "smokes/m455-melee-pursuit-set/smoke.properties");
        Properties m448 = load(root, "smokes/m448-creeper-fuse-set/smoke.properties");
        match(release, "version", "1.438.0");
        match(release, "milestone", "m448-creeper-fuse-set");
        same(release, "m453.signature", m453, "expected.signature");
        same(release, "server.sha256", m453, "server.jar.sha256");
        same(release, "m454.signature", m454, "expected.signature");
        same(release, "server.sha256", m454, "server.jar.sha256");
        same(release, "m455.signature", m455, "expected.signature");
        same(release, "server.sha256", m455, "server.jar.sha256");
        same(release, "m448.signature", m448, "expected.signature");
        same(release, "server.sha256", m448, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M453_PLAYER_DEATH_DROPS_SET.md", "docs/M453_CYCLE.md",
                "smokes/m453-player-death-drops-set/MAP.md", "docs/M454_PEACEFUL_DESPAWN_SET.md", "docs/M454_CYCLE.md",
                "smokes/m454-peaceful-despawn-set/MAP.md", "docs/M455_MELEE_PURSUIT_SET.md", "docs/M455_CYCLE.md",
                "smokes/m455-melee-pursuit-set/MAP.md", "docs/M448_CREEPER_FUSE_SET.md", "docs/M448_CYCLE.md",
                "smokes/m448-creeper-fuse-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.438.0 M448 Creeper fuse set GO");
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
