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
        Properties m279 = load(root, "smokes/m279-button-press/smoke.properties");
        Properties m280 = load(root, "smokes/m280-magenta-wool/smoke.properties");
        Properties m281 = load(root, "smokes/m281-light-blue-wool/smoke.properties");
        Properties m282 = load(root, "smokes/m282-lime-wool/smoke.properties");
        match(release, "version", "1.270.0");
        match(release, "milestone", "m282-lime-wool");
        same(release, "m279.signature", m279, "expected.signature");
        same(release, "server.sha256", m279, "server.jar.sha256");
        same(release, "m280.signature", m280, "expected.signature");
        same(release, "server.sha256", m280, "server.jar.sha256");
        same(release, "m281.signature", m281, "expected.signature");
        same(release, "server.sha256", m281, "server.jar.sha256");
        same(release, "m282.signature", m282, "expected.signature");
        same(release, "server.sha256", m282, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M279_BUTTON_PRESS.md", "docs/M279_CYCLE.md",
                "smokes/m279-button-press/MAP.md", "docs/M280_MAGENTA_WOOL.md", "docs/M280_CYCLE.md",
                "smokes/m280-magenta-wool/MAP.md", "docs/M281_LIGHT_BLUE_WOOL.md", "docs/M281_CYCLE.md",
                "smokes/m281-light-blue-wool/MAP.md", "docs/M282_LIME_WOOL.md", "docs/M282_CYCLE.md",
                "smokes/m282-lime-wool/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.270.0 M282 Lime wool GO");
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
