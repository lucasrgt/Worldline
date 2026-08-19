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
        Properties m209 = load(root, "smokes/m209-leaves/smoke.properties");
        Properties m210 = load(root, "smokes/m210-oak-planks/smoke.properties");
        Properties m211 = load(root, "smokes/m211-double-slab/smoke.properties");
        Properties m212 = load(root, "smokes/m212-gold-block/smoke.properties");
        match(release, "version", "1.200.0");
        match(release, "milestone", "m212-gold-block");
        same(release, "m209.signature", m209, "expected.signature");
        same(release, "server.sha256", m209, "server.jar.sha256");
        same(release, "m210.signature", m210, "expected.signature");
        same(release, "server.sha256", m210, "server.jar.sha256");
        same(release, "m211.signature", m211, "expected.signature");
        same(release, "server.sha256", m211, "server.jar.sha256");
        same(release, "m212.signature", m212, "expected.signature");
        same(release, "server.sha256", m212, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M209_LEAVES.md", "docs/M209_CYCLE.md",
                "smokes/m209-leaves/MAP.md", "docs/M210_OAK_PLANKS.md", "docs/M210_CYCLE.md",
                "smokes/m210-oak-planks/MAP.md", "docs/M211_DOUBLE_SLAB.md", "docs/M211_CYCLE.md",
                "smokes/m211-double-slab/MAP.md", "docs/M212_GOLD_BLOCK.md", "docs/M212_CYCLE.md",
                "smokes/m212-gold-block/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.200.0 M212 Gold block GO");
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
