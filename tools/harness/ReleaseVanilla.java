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
        Properties m212 = load(root, "smokes/m212-gold-block/smoke.properties");
        Properties m213 = load(root, "smokes/m213-iron-block/smoke.properties");
        Properties m214 = load(root, "smokes/m214-diamond-block/smoke.properties");
        Properties m215 = load(root, "smokes/m215-lapis-block/smoke.properties");
        match(release, "version", "1.203.0");
        match(release, "milestone", "m215-lapis-block");
        same(release, "m212.signature", m212, "expected.signature");
        same(release, "server.sha256", m212, "server.jar.sha256");
        same(release, "m213.signature", m213, "expected.signature");
        same(release, "server.sha256", m213, "server.jar.sha256");
        same(release, "m214.signature", m214, "expected.signature");
        same(release, "server.sha256", m214, "server.jar.sha256");
        same(release, "m215.signature", m215, "expected.signature");
        same(release, "server.sha256", m215, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M212_GOLD_BLOCK.md", "docs/M212_CYCLE.md",
                "smokes/m212-gold-block/MAP.md", "docs/M213_IRON_BLOCK.md", "docs/M213_CYCLE.md",
                "smokes/m213-iron-block/MAP.md", "docs/M214_DIAMOND_BLOCK.md", "docs/M214_CYCLE.md",
                "smokes/m214-diamond-block/MAP.md", "docs/M215_LAPIS_BLOCK.md", "docs/M215_CYCLE.md",
                "smokes/m215-lapis-block/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.203.0 M215 Lapis block GO");
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
