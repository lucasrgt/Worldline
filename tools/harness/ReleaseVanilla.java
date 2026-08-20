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
        Properties m297 = load(root, "smokes/m297-basic-crafts/smoke.properties");
        Properties m298 = load(root, "smokes/m298-wood-tool-crafts/smoke.properties");
        Properties m299 = load(root, "smokes/m299-stone-tool-crafts/smoke.properties");
        Properties m300 = load(root, "smokes/m300-ore-pick-breaks/smoke.properties");
        match(release, "version", "1.288.0");
        match(release, "milestone", "m300-ore-pick-breaks");
        same(release, "m297.signature", m297, "expected.signature");
        same(release, "server.sha256", m297, "server.jar.sha256");
        same(release, "m298.signature", m298, "expected.signature");
        same(release, "server.sha256", m298, "server.jar.sha256");
        same(release, "m299.signature", m299, "expected.signature");
        same(release, "server.sha256", m299, "server.jar.sha256");
        same(release, "m300.signature", m300, "expected.signature");
        same(release, "server.sha256", m300, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M297_BASIC_CRAFTS.md", "docs/M297_CYCLE.md",
                "smokes/m297-basic-crafts/MAP.md", "docs/M298_WOOD_TOOL_CRAFTS.md", "docs/M298_CYCLE.md",
                "smokes/m298-wood-tool-crafts/MAP.md", "docs/M299_STONE_TOOL_CRAFTS.md", "docs/M299_CYCLE.md",
                "smokes/m299-stone-tool-crafts/MAP.md", "docs/M300_ORE_PICK_BREAKS.md", "docs/M300_CYCLE.md",
                "smokes/m300-ore-pick-breaks/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.288.0 M300 Ore pick breaks GO");
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
