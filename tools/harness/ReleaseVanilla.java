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
        Properties m183 = load(root, "smokes/m183-rails/smoke.properties");
        Properties m184 = load(root, "smokes/m184-powered-rail/smoke.properties");
        Properties m185 = load(root, "smokes/m185-detector-rail/smoke.properties");
        Properties m186 = load(root, "smokes/m186-oak-stairs/smoke.properties");
        match(release, "version", "1.174.0");
        match(release, "milestone", "m186-oak-stairs");
        same(release, "m183.signature", m183, "expected.signature");
        same(release, "server.sha256", m183, "server.jar.sha256");
        same(release, "m184.signature", m184, "expected.signature");
        same(release, "server.sha256", m184, "server.jar.sha256");
        same(release, "m185.signature", m185, "expected.signature");
        same(release, "server.sha256", m185, "server.jar.sha256");
        same(release, "m186.signature", m186, "expected.signature");
        same(release, "server.sha256", m186, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M183_RAILS.md", "docs/M183_CYCLE.md",
                "smokes/m183-rails/MAP.md", "docs/M184_POWERED_RAIL.md", "docs/M184_CYCLE.md",
                "smokes/m184-powered-rail/MAP.md", "docs/M185_DETECTOR_RAIL.md", "docs/M185_CYCLE.md",
                "smokes/m185-detector-rail/MAP.md", "docs/M186_OAK_STAIRS.md", "docs/M186_CYCLE.md",
                "smokes/m186-oak-stairs/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.174.0 M186 Oak stairs GO");
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
