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
        Properties m204 = load(root, "smokes/m204-clay/smoke.properties");
        Properties m205 = load(root, "smokes/m205-brick/smoke.properties");
        Properties m206 = load(root, "smokes/m206-sponge/smoke.properties");
        Properties m207 = load(root, "smokes/m207-sandstone/smoke.properties");
        match(release, "version", "1.195.0");
        match(release, "milestone", "m207-sandstone");
        same(release, "m204.signature", m204, "expected.signature");
        same(release, "server.sha256", m204, "server.jar.sha256");
        same(release, "m205.signature", m205, "expected.signature");
        same(release, "server.sha256", m205, "server.jar.sha256");
        same(release, "m206.signature", m206, "expected.signature");
        same(release, "server.sha256", m206, "server.jar.sha256");
        same(release, "m207.signature", m207, "expected.signature");
        same(release, "server.sha256", m207, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M204_CLAY.md", "docs/M204_CYCLE.md",
                "smokes/m204-clay/MAP.md", "docs/M205_BRICK.md", "docs/M205_CYCLE.md",
                "smokes/m205-brick/MAP.md", "docs/M206_SPONGE.md", "docs/M206_CYCLE.md",
                "smokes/m206-sponge/MAP.md", "docs/M207_SANDSTONE.md", "docs/M207_CYCLE.md",
                "smokes/m207-sandstone/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.195.0 M207 Sandstone GO");
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
