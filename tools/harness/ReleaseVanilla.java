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
        Properties m158 = load(root, "smokes/m158-bed/smoke.properties");
        Properties m159 = load(root, "smokes/m159-sugar-cane/smoke.properties");
        Properties m160 = load(root, "smokes/m160-cake-eat/smoke.properties");
        Properties m161 = load(root, "smokes/m161-snowball/smoke.properties");
        match(release, "version", "1.149.0");
        match(release, "milestone", "m161-snowball");
        same(release, "m158.signature", m158, "expected.signature");
        same(release, "server.sha256", m158, "server.jar.sha256");
        same(release, "m159.signature", m159, "expected.signature");
        same(release, "server.sha256", m159, "server.jar.sha256");
        same(release, "m160.signature", m160, "expected.signature");
        same(release, "server.sha256", m160, "server.jar.sha256");
        same(release, "m161.signature", m161, "expected.signature");
        same(release, "server.sha256", m161, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M158_BED.md", "docs/M158_CYCLE.md",
                "smokes/m158-bed/MAP.md", "docs/M159_SUGAR_CANE.md", "docs/M159_CYCLE.md",
                "smokes/m159-sugar-cane/MAP.md", "docs/M160_CAKE_EAT.md", "docs/M160_CYCLE.md",
                "smokes/m160-cake-eat/MAP.md", "docs/M161_SNOWBALL.md", "docs/M161_CYCLE.md",
                "smokes/m161-snowball/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.149.0 M161 Snowball GO");
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
