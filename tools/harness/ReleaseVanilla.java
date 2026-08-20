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
        Properties m284 = load(root, "smokes/m284-gray-wool/smoke.properties");
        Properties m285 = load(root, "smokes/m285-light-gray-wool/smoke.properties");
        Properties m286 = load(root, "smokes/m286-cyan-wool/smoke.properties");
        Properties m287 = load(root, "smokes/m287-purple-wool/smoke.properties");
        match(release, "version", "1.275.0");
        match(release, "milestone", "m287-purple-wool");
        same(release, "m284.signature", m284, "expected.signature");
        same(release, "server.sha256", m284, "server.jar.sha256");
        same(release, "m285.signature", m285, "expected.signature");
        same(release, "server.sha256", m285, "server.jar.sha256");
        same(release, "m286.signature", m286, "expected.signature");
        same(release, "server.sha256", m286, "server.jar.sha256");
        same(release, "m287.signature", m287, "expected.signature");
        same(release, "server.sha256", m287, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M284_GRAY_WOOL.md", "docs/M284_CYCLE.md",
                "smokes/m284-gray-wool/MAP.md", "docs/M285_LIGHT_GRAY_WOOL.md", "docs/M285_CYCLE.md",
                "smokes/m285-light-gray-wool/MAP.md", "docs/M286_CYAN_WOOL.md", "docs/M286_CYCLE.md",
                "smokes/m286-cyan-wool/MAP.md", "docs/M287_PURPLE_WOOL.md", "docs/M287_CYCLE.md",
                "smokes/m287-purple-wool/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.275.0 M287 Purple wool GO");
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
