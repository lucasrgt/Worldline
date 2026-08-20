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
        Properties m253 = load(root, "smokes/m253-green-wool/smoke.properties");
        Properties m254 = load(root, "smokes/m254-water-place/smoke.properties");
        Properties m255 = load(root, "smokes/m255-lava-place/smoke.properties");
        Properties m256 = load(root, "smokes/m256-chest-minecart/smoke.properties");
        match(release, "version", "1.244.0");
        match(release, "milestone", "m256-chest-minecart");
        same(release, "m253.signature", m253, "expected.signature");
        same(release, "server.sha256", m253, "server.jar.sha256");
        same(release, "m254.signature", m254, "expected.signature");
        same(release, "server.sha256", m254, "server.jar.sha256");
        same(release, "m255.signature", m255, "expected.signature");
        same(release, "server.sha256", m255, "server.jar.sha256");
        same(release, "m256.signature", m256, "expected.signature");
        same(release, "server.sha256", m256, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M253_GREEN_WOOL.md", "docs/M253_CYCLE.md",
                "smokes/m253-green-wool/MAP.md", "docs/M254_WATER_PLACE.md", "docs/M254_CYCLE.md",
                "smokes/m254-water-place/MAP.md", "docs/M255_LAVA_PLACE.md", "docs/M255_CYCLE.md",
                "smokes/m255-lava-place/MAP.md", "docs/M256_CHEST_MINECART.md", "docs/M256_CYCLE.md",
                "smokes/m256-chest-minecart/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.244.0 M256 Chest minecart GO");
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
