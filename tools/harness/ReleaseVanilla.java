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
        Properties m391 = load(root, "smokes/m391-creeper-explode-set/smoke.properties");
        Properties m392 = load(root, "smokes/m392-remaining-fluid-flow/smoke.properties");
        Properties m393 = load(root, "smokes/m393-stair-facing-set/smoke.properties");
        Properties m394 = load(root, "smokes/m394-remaining-slab-place/smoke.properties");
        match(release, "version", "1.382.0");
        match(release, "milestone", "m394-remaining-slab-place");
        same(release, "m391.signature", m391, "expected.signature");
        same(release, "server.sha256", m391, "server.jar.sha256");
        same(release, "m392.signature", m392, "expected.signature");
        same(release, "server.sha256", m392, "server.jar.sha256");
        same(release, "m393.signature", m393, "expected.signature");
        same(release, "server.sha256", m393, "server.jar.sha256");
        same(release, "m394.signature", m394, "expected.signature");
        same(release, "server.sha256", m394, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M391_CREEPER_EXPLODE_SET.md", "docs/M391_CYCLE.md",
                "smokes/m391-creeper-explode-set/MAP.md", "docs/M392_REMAINING_FLUID_FLOW.md", "docs/M392_CYCLE.md",
                "smokes/m392-remaining-fluid-flow/MAP.md", "docs/M393_STAIR_FACING_SET.md", "docs/M393_CYCLE.md",
                "smokes/m393-stair-facing-set/MAP.md", "docs/M394_REMAINING_SLAB_PLACE.md", "docs/M394_CYCLE.md",
                "smokes/m394-remaining-slab-place/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.382.0 M394 Remaining slab place GO");
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
