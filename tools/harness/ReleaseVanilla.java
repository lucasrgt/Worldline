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
        Properties m174 = load(root, "smokes/m174-ladder/smoke.properties");
        Properties m175 = load(root, "smokes/m175-torch/smoke.properties");
        Properties m176 = load(root, "smokes/m176-sign/smoke.properties");
        Properties m177 = load(root, "smokes/m177-painting/smoke.properties");
        match(release, "version", "1.165.0");
        match(release, "milestone", "m177-painting");
        same(release, "m174.signature", m174, "expected.signature");
        same(release, "server.sha256", m174, "server.jar.sha256");
        same(release, "m175.signature", m175, "expected.signature");
        same(release, "server.sha256", m175, "server.jar.sha256");
        same(release, "m176.signature", m176, "expected.signature");
        same(release, "server.sha256", m176, "server.jar.sha256");
        same(release, "m177.signature", m177, "expected.signature");
        same(release, "server.sha256", m177, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M174_LADDER.md", "docs/M174_CYCLE.md",
                "smokes/m174-ladder/MAP.md", "docs/M175_TORCH.md", "docs/M175_CYCLE.md",
                "smokes/m175-torch/MAP.md", "docs/M176_SIGN.md", "docs/M176_CYCLE.md",
                "smokes/m176-sign/MAP.md", "docs/M177_PAINTING.md", "docs/M177_CYCLE.md",
                "smokes/m177-painting/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.165.0 M177 Painting GO");
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
