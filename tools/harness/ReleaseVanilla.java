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
        Properties m150 = load(root, "smokes/m150-pig-pork-drop/smoke.properties");
        Properties m151 = load(root, "smokes/m151-netherrack-fire/smoke.properties");
        Properties m152 = load(root, "smokes/m152-fire-wool-consumption/smoke.properties");
        match(release, "version", "1.140.0");
        match(release, "milestone", "m152-fire-wool-consumption");
        same(release, "m150.signature", m150, "expected.signature");
        same(release, "server.sha256", m150, "server.jar.sha256");
        same(release, "m151.signature", m151, "expected.signature");
        same(release, "server.sha256", m151, "server.jar.sha256");
        same(release, "m152.signature", m152, "expected.signature");
        same(release, "server.sha256", m152, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M150_PIG_PORK_DROP.md", "docs/M150_CYCLE.md",
                "smokes/m150-pig-pork-drop/MAP.md", "docs/M151_NETHERRACK_FIRE.md", "docs/M151_CYCLE.md",
                "smokes/m151-netherrack-fire/MAP.md", "docs/M152_FIRE_WOOL_CONSUMPTION.md", "docs/M152_CYCLE.md",
                "smokes/m152-fire-wool-consumption/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.140.0 M152 Fire-wool-consumption GO");
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
