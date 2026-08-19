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
        Properties m155 = load(root, "smokes/m155-minecart-spawn/smoke.properties");
        Properties m156 = load(root, "smokes/m156-farmland-hydration/smoke.properties");
        Properties m157 = load(root, "smokes/m157-bow-arrow/smoke.properties");
        Properties m158 = load(root, "smokes/m158-bed/smoke.properties");
        match(release, "version", "1.146.0");
        match(release, "milestone", "m158-bed");
        same(release, "m155.signature", m155, "expected.signature");
        same(release, "server.sha256", m155, "server.jar.sha256");
        same(release, "m156.signature", m156, "expected.signature");
        same(release, "server.sha256", m156, "server.jar.sha256");
        same(release, "m157.signature", m157, "expected.signature");
        same(release, "server.sha256", m157, "server.jar.sha256");
        same(release, "m158.signature", m158, "expected.signature");
        same(release, "server.sha256", m158, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M155_MINECART_SPAWN.md", "docs/M155_CYCLE.md",
                "smokes/m155-minecart-spawn/MAP.md", "docs/M156_FARMLAND_HYDRATION.md", "docs/M156_CYCLE.md",
                "smokes/m156-farmland-hydration/MAP.md", "docs/M157_BOW_ARROW.md", "docs/M157_CYCLE.md",
                "smokes/m157-bow-arrow/MAP.md", "docs/M158_BED.md", "docs/M158_CYCLE.md",
                "smokes/m158-bed/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.146.0 M158 Bed GO");
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
