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
        Properties m394 = load(root, "smokes/m394-remaining-slab-place/smoke.properties");
        Properties m395 = load(root, "smokes/m395-remaining-dye-mix/smoke.properties");
        Properties m396 = load(root, "smokes/m396-remaining-wool-crafts/smoke.properties");
        Properties m397 = load(root, "smokes/m397-dispenser-projectiles/smoke.properties");
        match(release, "version", "1.385.0");
        match(release, "milestone", "m397-dispenser-projectiles");
        same(release, "m394.signature", m394, "expected.signature");
        same(release, "server.sha256", m394, "server.jar.sha256");
        same(release, "m395.signature", m395, "expected.signature");
        same(release, "server.sha256", m395, "server.jar.sha256");
        same(release, "m396.signature", m396, "expected.signature");
        same(release, "server.sha256", m396, "server.jar.sha256");
        same(release, "m397.signature", m397, "expected.signature");
        same(release, "server.sha256", m397, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M394_REMAINING_SLAB_PLACE.md", "docs/M394_CYCLE.md",
                "smokes/m394-remaining-slab-place/MAP.md", "docs/M395_REMAINING_DYE_MIX.md", "docs/M395_CYCLE.md",
                "smokes/m395-remaining-dye-mix/MAP.md", "docs/M396_REMAINING_WOOL_CRAFTS.md", "docs/M396_CYCLE.md",
                "smokes/m396-remaining-wool-crafts/MAP.md", "docs/M397_DISPENSER_PROJECTILES.md", "docs/M397_CYCLE.md",
                "smokes/m397-dispenser-projectiles/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.385.0 M397 Dispenser projectiles GO");
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
