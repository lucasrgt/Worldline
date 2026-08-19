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
        Properties m207 = load(root, "smokes/m207-sandstone/smoke.properties");
        Properties m208 = load(root, "smokes/m208-oak-log/smoke.properties");
        Properties m209 = load(root, "smokes/m209-leaves/smoke.properties");
        Properties m210 = load(root, "smokes/m210-oak-planks/smoke.properties");
        match(release, "version", "1.198.0");
        match(release, "milestone", "m210-oak-planks");
        same(release, "m207.signature", m207, "expected.signature");
        same(release, "server.sha256", m207, "server.jar.sha256");
        same(release, "m208.signature", m208, "expected.signature");
        same(release, "server.sha256", m208, "server.jar.sha256");
        same(release, "m209.signature", m209, "expected.signature");
        same(release, "server.sha256", m209, "server.jar.sha256");
        same(release, "m210.signature", m210, "expected.signature");
        same(release, "server.sha256", m210, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M207_SANDSTONE.md", "docs/M207_CYCLE.md",
                "smokes/m207-sandstone/MAP.md", "docs/M208_OAK_LOG.md", "docs/M208_CYCLE.md",
                "smokes/m208-oak-log/MAP.md", "docs/M209_LEAVES.md", "docs/M209_CYCLE.md",
                "smokes/m209-leaves/MAP.md", "docs/M210_OAK_PLANKS.md", "docs/M210_CYCLE.md",
                "smokes/m210-oak-planks/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.198.0 M210 Oak planks GO");
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
