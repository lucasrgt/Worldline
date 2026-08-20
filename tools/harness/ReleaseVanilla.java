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
        Properties m405 = load(root, "smokes/m405-pig-saddle-set/smoke.properties");
        Properties m406 = load(root, "smokes/m406-sheep-dye-set/smoke.properties");
        Properties m407 = load(root, "smokes/m407-chicken-egg-set/smoke.properties");
        Properties m408 = load(root, "smokes/m408-squid-ink-set/smoke.properties");
        match(release, "version", "1.396.0");
        match(release, "milestone", "m408-squid-ink-set");
        same(release, "m405.signature", m405, "expected.signature");
        same(release, "server.sha256", m405, "server.jar.sha256");
        same(release, "m406.signature", m406, "expected.signature");
        same(release, "server.sha256", m406, "server.jar.sha256");
        same(release, "m407.signature", m407, "expected.signature");
        same(release, "server.sha256", m407, "server.jar.sha256");
        same(release, "m408.signature", m408, "expected.signature");
        same(release, "server.sha256", m408, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M405_PIG_SADDLE_SET.md", "docs/M405_CYCLE.md",
                "smokes/m405-pig-saddle-set/MAP.md", "docs/M406_SHEEP_DYE_SET.md", "docs/M406_CYCLE.md",
                "smokes/m406-sheep-dye-set/MAP.md", "docs/M407_CHICKEN_EGG_SET.md", "docs/M407_CYCLE.md",
                "smokes/m407-chicken-egg-set/MAP.md", "docs/M408_SQUID_INK_SET.md", "docs/M408_CYCLE.md",
                "smokes/m408-squid-ink-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.396.0 M408 Squid ink set GO");
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
