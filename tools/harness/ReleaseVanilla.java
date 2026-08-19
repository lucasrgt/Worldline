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
        Properties m229 = load(root, "smokes/m229-redstone-ore/smoke.properties");
        Properties m230 = load(root, "smokes/m230-lapis-ore/smoke.properties");
        Properties m231 = load(root, "smokes/m231-dispenser-place/smoke.properties");
        Properties m232 = load(root, "smokes/m232-chest-place/smoke.properties");
        match(release, "version", "1.220.0");
        match(release, "milestone", "m232-chest-place");
        same(release, "m229.signature", m229, "expected.signature");
        same(release, "server.sha256", m229, "server.jar.sha256");
        same(release, "m230.signature", m230, "expected.signature");
        same(release, "server.sha256", m230, "server.jar.sha256");
        same(release, "m231.signature", m231, "expected.signature");
        same(release, "server.sha256", m231, "server.jar.sha256");
        same(release, "m232.signature", m232, "expected.signature");
        same(release, "server.sha256", m232, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M229_REDSTONE_ORE.md", "docs/M229_CYCLE.md",
                "smokes/m229-redstone-ore/MAP.md", "docs/M230_LAPIS_ORE.md", "docs/M230_CYCLE.md",
                "smokes/m230-lapis-ore/MAP.md", "docs/M231_DISPENSER_PLACE.md", "docs/M231_CYCLE.md",
                "smokes/m231-dispenser-place/MAP.md", "docs/M232_CHEST_PLACE.md", "docs/M232_CYCLE.md",
                "smokes/m232-chest-place/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.220.0 M232 Chest place GO");
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
