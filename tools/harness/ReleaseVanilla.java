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
        Properties m169 = load(root, "smokes/m169-egg-throw/smoke.properties");
        Properties m170 = load(root, "smokes/m170-repeater/smoke.properties");
        Properties m171 = load(root, "smokes/m171-pumpkin/smoke.properties");
        Properties m172 = load(root, "smokes/m172-wooden-plate/smoke.properties");
        match(release, "version", "1.160.0");
        match(release, "milestone", "m172-wooden-plate");
        same(release, "m169.signature", m169, "expected.signature");
        same(release, "server.sha256", m169, "server.jar.sha256");
        same(release, "m170.signature", m170, "expected.signature");
        same(release, "server.sha256", m170, "server.jar.sha256");
        same(release, "m171.signature", m171, "expected.signature");
        same(release, "server.sha256", m171, "server.jar.sha256");
        same(release, "m172.signature", m172, "expected.signature");
        same(release, "server.sha256", m172, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M169_EGG_THROW.md", "docs/M169_CYCLE.md",
                "smokes/m169-egg-throw/MAP.md", "docs/M170_REPEATER.md", "docs/M170_CYCLE.md",
                "smokes/m170-repeater/MAP.md", "docs/M171_PUMPKIN.md", "docs/M171_CYCLE.md",
                "smokes/m171-pumpkin/MAP.md", "docs/M172_WOODEN_PLATE.md", "docs/M172_CYCLE.md",
                "smokes/m172-wooden-plate/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.160.0 M172 Wooden-plate GO");
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
