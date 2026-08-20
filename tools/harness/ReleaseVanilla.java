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
        Properties m419 = load(root, "smokes/m419-remaining-netherrack-place/smoke.properties");
        Properties m413 = load(root, "smokes/m413-fire-spread-set/smoke.properties");
        Properties m433 = load(root, "smokes/m433-remaining-chest-orient-set/smoke.properties");
        Properties m434 = load(root, "smokes/m434-remaining-sponge-glass-ice/smoke.properties");
        match(release, "version", "1.409.0");
        match(release, "milestone", "m434-remaining-sponge-glass-ice");
        same(release, "m419.signature", m419, "expected.signature");
        same(release, "server.sha256", m419, "server.jar.sha256");
        same(release, "m413.signature", m413, "expected.signature");
        same(release, "server.sha256", m413, "server.jar.sha256");
        same(release, "m433.signature", m433, "expected.signature");
        same(release, "server.sha256", m433, "server.jar.sha256");
        same(release, "m434.signature", m434, "expected.signature");
        same(release, "server.sha256", m434, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M419_REMAINING_NETHERRACK_PLACE.md", "docs/M419_CYCLE.md",
                "smokes/m419-remaining-netherrack-place/MAP.md", "docs/M413_FIRE_SPREAD_SET.md", "docs/M413_CYCLE.md",
                "smokes/m413-fire-spread-set/MAP.md", "docs/M433_REMAINING_CHEST_ORIENT_SET.md", "docs/M433_CYCLE.md",
                "smokes/m433-remaining-chest-orient-set/MAP.md", "docs/M434_REMAINING_SPONGE_GLASS_ICE.md", "docs/M434_CYCLE.md",
                "smokes/m434-remaining-sponge-glass-ice/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.409.0 M434 Remaining sponge glass ice GO");
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
