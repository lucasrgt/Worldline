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
        Properties m184 = load(root, "smokes/m184-powered-rail/smoke.properties");
        Properties m185 = load(root, "smokes/m185-detector-rail/smoke.properties");
        Properties m186 = load(root, "smokes/m186-oak-stairs/smoke.properties");
        Properties m187 = load(root, "smokes/m187-cobble-stairs/smoke.properties");
        match(release, "version", "1.175.0");
        match(release, "milestone", "m187-cobble-stairs");
        same(release, "m184.signature", m184, "expected.signature");
        same(release, "server.sha256", m184, "server.jar.sha256");
        same(release, "m185.signature", m185, "expected.signature");
        same(release, "server.sha256", m185, "server.jar.sha256");
        same(release, "m186.signature", m186, "expected.signature");
        same(release, "server.sha256", m186, "server.jar.sha256");
        same(release, "m187.signature", m187, "expected.signature");
        same(release, "server.sha256", m187, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M184_POWERED_RAIL.md", "docs/M184_CYCLE.md",
                "smokes/m184-powered-rail/MAP.md", "docs/M185_DETECTOR_RAIL.md", "docs/M185_CYCLE.md",
                "smokes/m185-detector-rail/MAP.md", "docs/M186_OAK_STAIRS.md", "docs/M186_CYCLE.md",
                "smokes/m186-oak-stairs/MAP.md", "docs/M187_COBBLE_STAIRS.md", "docs/M187_CYCLE.md",
                "smokes/m187-cobble-stairs/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.175.0 M187 Cobble stairs GO");
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
