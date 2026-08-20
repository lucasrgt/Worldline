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
        Properties m396 = load(root, "smokes/m396-remaining-wool-crafts/smoke.properties");
        Properties m397 = load(root, "smokes/m397-dispenser-projectiles/smoke.properties");
        Properties m398 = load(root, "smokes/m398-jukebox-eject-set/smoke.properties");
        Properties m399 = load(root, "smokes/m399-wooden-button-set/smoke.properties");
        match(release, "version", "1.387.0");
        match(release, "milestone", "m399-wooden-button-set");
        same(release, "m396.signature", m396, "expected.signature");
        same(release, "server.sha256", m396, "server.jar.sha256");
        same(release, "m397.signature", m397, "expected.signature");
        same(release, "server.sha256", m397, "server.jar.sha256");
        same(release, "m398.signature", m398, "expected.signature");
        same(release, "server.sha256", m398, "server.jar.sha256");
        same(release, "m399.signature", m399, "expected.signature");
        same(release, "server.sha256", m399, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M396_REMAINING_WOOL_CRAFTS.md", "docs/M396_CYCLE.md",
                "smokes/m396-remaining-wool-crafts/MAP.md", "docs/M397_DISPENSER_PROJECTILES.md", "docs/M397_CYCLE.md",
                "smokes/m397-dispenser-projectiles/MAP.md", "docs/M398_JUKEBOX_EJECT_SET.md", "docs/M398_CYCLE.md",
                "smokes/m398-jukebox-eject-set/MAP.md", "docs/M399_WOODEN_BUTTON_SET.md", "docs/M399_CYCLE.md",
                "smokes/m399-wooden-button-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.387.0 M399 Wooden button set GO");
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
