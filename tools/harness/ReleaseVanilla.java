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
        Properties m361 = load(root, "smokes/m361-ladder-climb-set/smoke.properties");
        Properties m362 = load(root, "smokes/m362-fence-collision-set/smoke.properties");
        Properties m363 = load(root, "smokes/m363-hostile-identity-set/smoke.properties");
        Properties m364 = load(root, "smokes/m364-light-opacity-set/smoke.properties");
        match(release, "version", "1.352.0");
        match(release, "milestone", "m364-light-opacity-set");
        same(release, "m361.signature", m361, "expected.signature");
        same(release, "server.sha256", m361, "server.jar.sha256");
        same(release, "m362.signature", m362, "expected.signature");
        same(release, "server.sha256", m362, "server.jar.sha256");
        same(release, "m363.signature", m363, "expected.signature");
        same(release, "server.sha256", m363, "server.jar.sha256");
        same(release, "m364.signature", m364, "expected.signature");
        same(release, "server.sha256", m364, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M361_LADDER_CLIMB_SET.md", "docs/M361_CYCLE.md",
                "smokes/m361-ladder-climb-set/MAP.md", "docs/M362_FENCE_COLLISION_SET.md", "docs/M362_CYCLE.md",
                "smokes/m362-fence-collision-set/MAP.md", "docs/M363_HOSTILE_IDENTITY_SET.md", "docs/M363_CYCLE.md",
                "smokes/m363-hostile-identity-set/MAP.md", "docs/M364_LIGHT_OPACITY_SET.md", "docs/M364_CYCLE.md",
                "smokes/m364-light-opacity-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.352.0 M364 Light opacity set GO");
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
