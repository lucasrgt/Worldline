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
        Properties m347 = load(root, "smokes/m347-gold-diamond-hoes/smoke.properties");
        Properties m348 = load(root, "smokes/m348-dye-mix-crafts/smoke.properties");
        Properties m349 = load(root, "smokes/m349-double-chest-set/smoke.properties");
        Properties m350 = load(root, "smokes/m350-sign-text-set/smoke.properties");
        match(release, "version", "1.338.0");
        match(release, "milestone", "m350-sign-text-set");
        same(release, "m347.signature", m347, "expected.signature");
        same(release, "server.sha256", m347, "server.jar.sha256");
        same(release, "m348.signature", m348, "expected.signature");
        same(release, "server.sha256", m348, "server.jar.sha256");
        same(release, "m349.signature", m349, "expected.signature");
        same(release, "server.sha256", m349, "server.jar.sha256");
        same(release, "m350.signature", m350, "expected.signature");
        same(release, "server.sha256", m350, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M347_GOLD_DIAMOND_HOES.md", "docs/M347_CYCLE.md",
                "smokes/m347-gold-diamond-hoes/MAP.md", "docs/M348_DYE_MIX_CRAFTS.md", "docs/M348_CYCLE.md",
                "smokes/m348-dye-mix-crafts/MAP.md", "docs/M349_DOUBLE_CHEST_SET.md", "docs/M349_CYCLE.md",
                "smokes/m349-double-chest-set/MAP.md", "docs/M350_SIGN_TEXT_SET.md", "docs/M350_CYCLE.md",
                "smokes/m350-sign-text-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.338.0 M350 Sign text set GO");
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
