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
        Properties m557 = load(root, "smokes/m557-one-tick-pulse-set/smoke.properties");
        Properties m566 = load(root, "smokes/m566-grass-spread-set/smoke.properties");
        Properties m555 = load(root, "smokes/m555-torch-burnout-set/smoke.properties");
        Properties m567 = load(root, "smokes/m567-bed-spawn-set/smoke.properties");
        match(release, "version", "1.461.0");
        match(release, "milestone", "m567-bed-spawn-set");
        same(release, "m557.signature", m557, "expected.signature");
        same(release, "server.sha256", m557, "server.jar.sha256");
        same(release, "m566.signature", m566, "expected.signature");
        same(release, "server.sha256", m566, "server.jar.sha256");
        same(release, "m555.signature", m555, "expected.signature");
        same(release, "server.sha256", m555, "server.jar.sha256");
        same(release, "m567.signature", m567, "expected.signature");
        same(release, "server.sha256", m567, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M557_ONE_TICK_PULSE_SET.md", "docs/M557_CYCLE.md",
                "smokes/m557-one-tick-pulse-set/MAP.md", "docs/M566_GRASS_SPREAD_SET.md", "docs/M566_CYCLE.md",
                "smokes/m566-grass-spread-set/MAP.md", "docs/M555_TORCH_BURNOUT_SET.md", "docs/M555_CYCLE.md",
                "smokes/m555-torch-burnout-set/MAP.md", "docs/M567_BED_SPAWN_SET.md", "docs/M567_CYCLE.md",
                "smokes/m567-bed-spawn-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.461.0 M567 Bed spawn set GO");
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
