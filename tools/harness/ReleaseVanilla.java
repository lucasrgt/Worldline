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
        Properties m370 = load(root, "smokes/m370-remaining-furnace-smelts/smoke.properties");
        Properties m371 = load(root, "smokes/m371-machine-block-crafts/smoke.properties");
        Properties m372 = load(root, "smokes/m372-placeable-item-crafts/smoke.properties");
        Properties m373 = load(root, "smokes/m373-milk-bucket-set/smoke.properties");
        match(release, "version", "1.361.0");
        match(release, "milestone", "m373-milk-bucket-set");
        same(release, "m370.signature", m370, "expected.signature");
        same(release, "server.sha256", m370, "server.jar.sha256");
        same(release, "m371.signature", m371, "expected.signature");
        same(release, "server.sha256", m371, "server.jar.sha256");
        same(release, "m372.signature", m372, "expected.signature");
        same(release, "server.sha256", m372, "server.jar.sha256");
        same(release, "m373.signature", m373, "expected.signature");
        same(release, "server.sha256", m373, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M370_REMAINING_FURNACE_SMELTS.md", "docs/M370_CYCLE.md",
                "smokes/m370-remaining-furnace-smelts/MAP.md", "docs/M371_MACHINE_BLOCK_CRAFTS.md", "docs/M371_CYCLE.md",
                "smokes/m371-machine-block-crafts/MAP.md", "docs/M372_PLACEABLE_ITEM_CRAFTS.md", "docs/M372_CYCLE.md",
                "smokes/m372-placeable-item-crafts/MAP.md", "docs/M373_MILK_BUCKET_SET.md", "docs/M373_CYCLE.md",
                "smokes/m373-milk-bucket-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.361.0 M373 Milk bucket set GO");
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
