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
        Properties m265 = load(root, "smokes/m265-fish-eat/smoke.properties");
        Properties m266 = load(root, "smokes/m266-cooked-fish-eat/smoke.properties");
        Properties m267 = load(root, "smokes/m267-milk-bucket/smoke.properties");
        Properties m268 = load(root, "smokes/m268-flint-steel-fire/smoke.properties");
        match(release, "version", "1.256.0");
        match(release, "milestone", "m268-flint-steel-fire");
        same(release, "m265.signature", m265, "expected.signature");
        same(release, "server.sha256", m265, "server.jar.sha256");
        same(release, "m266.signature", m266, "expected.signature");
        same(release, "server.sha256", m266, "server.jar.sha256");
        same(release, "m267.signature", m267, "expected.signature");
        same(release, "server.sha256", m267, "server.jar.sha256");
        same(release, "m268.signature", m268, "expected.signature");
        same(release, "server.sha256", m268, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M265_FISH_EAT.md", "docs/M265_CYCLE.md",
                "smokes/m265-fish-eat/MAP.md", "docs/M266_COOKED_FISH_EAT.md", "docs/M266_CYCLE.md",
                "smokes/m266-cooked-fish-eat/MAP.md", "docs/M267_MILK_BUCKET.md", "docs/M267_CYCLE.md",
                "smokes/m267-milk-bucket/MAP.md", "docs/M268_FLINT_STEEL_FIRE.md", "docs/M268_CYCLE.md",
                "smokes/m268-flint-steel-fire/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.256.0 M268 Flint steel fire GO");
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
