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
        Properties m181 = load(root, "smokes/m181-lava-bucket/smoke.properties");
        Properties m182 = load(root, "smokes/m182-redstone-torch/smoke.properties");
        Properties m183 = load(root, "smokes/m183-rails/smoke.properties");
        Properties m184 = load(root, "smokes/m184-powered-rail/smoke.properties");
        match(release, "version", "1.172.0");
        match(release, "milestone", "m184-powered-rail");
        same(release, "m181.signature", m181, "expected.signature");
        same(release, "server.sha256", m181, "server.jar.sha256");
        same(release, "m182.signature", m182, "expected.signature");
        same(release, "server.sha256", m182, "server.jar.sha256");
        same(release, "m183.signature", m183, "expected.signature");
        same(release, "server.sha256", m183, "server.jar.sha256");
        same(release, "m184.signature", m184, "expected.signature");
        same(release, "server.sha256", m184, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M181_LAVA_BUCKET.md", "docs/M181_CYCLE.md",
                "smokes/m181-lava-bucket/MAP.md", "docs/M182_REDSTONE_TORCH.md", "docs/M182_CYCLE.md",
                "smokes/m182-redstone-torch/MAP.md", "docs/M183_RAILS.md", "docs/M183_CYCLE.md",
                "smokes/m183-rails/MAP.md", "docs/M184_POWERED_RAIL.md", "docs/M184_CYCLE.md",
                "smokes/m184-powered-rail/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.172.0 M184 Powered rail GO");
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
