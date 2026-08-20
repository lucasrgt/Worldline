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
        Properties m272 = load(root, "smokes/m272-diamond-leggings/smoke.properties");
        Properties m273 = load(root, "smokes/m273-chain-boots/smoke.properties");
        Properties m274 = load(root, "smokes/m274-falling-gravel/smoke.properties");
        Properties m275 = load(root, "smokes/m275-cactus-damage/smoke.properties");
        match(release, "version", "1.263.0");
        match(release, "milestone", "m275-cactus-damage");
        same(release, "m272.signature", m272, "expected.signature");
        same(release, "server.sha256", m272, "server.jar.sha256");
        same(release, "m273.signature", m273, "expected.signature");
        same(release, "server.sha256", m273, "server.jar.sha256");
        same(release, "m274.signature", m274, "expected.signature");
        same(release, "server.sha256", m274, "server.jar.sha256");
        same(release, "m275.signature", m275, "expected.signature");
        same(release, "server.sha256", m275, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M272_DIAMOND_LEGGINGS.md", "docs/M272_CYCLE.md",
                "smokes/m272-diamond-leggings/MAP.md", "docs/M273_CHAIN_BOOTS.md", "docs/M273_CYCLE.md",
                "smokes/m273-chain-boots/MAP.md", "docs/M274_FALLING_GRAVEL.md", "docs/M274_CYCLE.md",
                "smokes/m274-falling-gravel/MAP.md", "docs/M275_CACTUS_DAMAGE.md", "docs/M275_CYCLE.md",
                "smokes/m275-cactus-damage/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.263.0 M275 Cactus damage GO");
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
