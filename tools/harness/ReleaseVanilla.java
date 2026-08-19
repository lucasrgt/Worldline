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
        Properties m231 = load(root, "smokes/m231-dispenser-place/smoke.properties");
        Properties m232 = load(root, "smokes/m232-chest-place/smoke.properties");
        Properties m233 = load(root, "smokes/m233-note-block-place/smoke.properties");
        Properties m234 = load(root, "smokes/m234-sandstone-slab/smoke.properties");
        match(release, "version", "1.222.0");
        match(release, "milestone", "m234-sandstone-slab");
        same(release, "m231.signature", m231, "expected.signature");
        same(release, "server.sha256", m231, "server.jar.sha256");
        same(release, "m232.signature", m232, "expected.signature");
        same(release, "server.sha256", m232, "server.jar.sha256");
        same(release, "m233.signature", m233, "expected.signature");
        same(release, "server.sha256", m233, "server.jar.sha256");
        same(release, "m234.signature", m234, "expected.signature");
        same(release, "server.sha256", m234, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M231_DISPENSER_PLACE.md", "docs/M231_CYCLE.md",
                "smokes/m231-dispenser-place/MAP.md", "docs/M232_CHEST_PLACE.md", "docs/M232_CYCLE.md",
                "smokes/m232-chest-place/MAP.md", "docs/M233_NOTE_BLOCK_PLACE.md", "docs/M233_CYCLE.md",
                "smokes/m233-note-block-place/MAP.md", "docs/M234_SANDSTONE_SLAB.md", "docs/M234_CYCLE.md",
                "smokes/m234-sandstone-slab/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.222.0 M234 Sandstone slab GO");
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
