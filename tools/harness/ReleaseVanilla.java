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
        Properties m277 = load(root, "smokes/m277-wooden-door-open/smoke.properties");
        Properties m278 = load(root, "smokes/m278-trapdoor-toggle/smoke.properties");
        Properties m279 = load(root, "smokes/m279-button-press/smoke.properties");
        Properties m280 = load(root, "smokes/m280-magenta-wool/smoke.properties");
        match(release, "version", "1.268.0");
        match(release, "milestone", "m280-magenta-wool");
        same(release, "m277.signature", m277, "expected.signature");
        same(release, "server.sha256", m277, "server.jar.sha256");
        same(release, "m278.signature", m278, "expected.signature");
        same(release, "server.sha256", m278, "server.jar.sha256");
        same(release, "m279.signature", m279, "expected.signature");
        same(release, "server.sha256", m279, "server.jar.sha256");
        same(release, "m280.signature", m280, "expected.signature");
        same(release, "server.sha256", m280, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M277_WOODEN_DOOR_OPEN.md", "docs/M277_CYCLE.md",
                "smokes/m277-wooden-door-open/MAP.md", "docs/M278_TRAPDOOR_TOGGLE.md", "docs/M278_CYCLE.md",
                "smokes/m278-trapdoor-toggle/MAP.md", "docs/M279_BUTTON_PRESS.md", "docs/M279_CYCLE.md",
                "smokes/m279-button-press/MAP.md", "docs/M280_MAGENTA_WOOL.md", "docs/M280_CYCLE.md",
                "smokes/m280-magenta-wool/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.268.0 M280 Magenta wool GO");
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
