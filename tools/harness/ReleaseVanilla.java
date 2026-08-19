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
        Properties m220 = load(root, "smokes/m220-workbench/smoke.properties");
        Properties m221 = load(root, "smokes/m221-furnace/smoke.properties");
        Properties m222 = load(root, "smokes/m222-cobble/smoke.properties");
        Properties m223 = load(root, "smokes/m223-dirt/smoke.properties");
        match(release, "version", "1.211.0");
        match(release, "milestone", "m223-dirt");
        same(release, "m220.signature", m220, "expected.signature");
        same(release, "server.sha256", m220, "server.jar.sha256");
        same(release, "m221.signature", m221, "expected.signature");
        same(release, "server.sha256", m221, "server.jar.sha256");
        same(release, "m222.signature", m222, "expected.signature");
        same(release, "server.sha256", m222, "server.jar.sha256");
        same(release, "m223.signature", m223, "expected.signature");
        same(release, "server.sha256", m223, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M220_WORKBENCH.md", "docs/M220_CYCLE.md",
                "smokes/m220-workbench/MAP.md", "docs/M221_FURNACE.md", "docs/M221_CYCLE.md",
                "smokes/m221-furnace/MAP.md", "docs/M222_COBBLE.md", "docs/M222_CYCLE.md",
                "smokes/m222-cobble/MAP.md", "docs/M223_DIRT.md", "docs/M223_CYCLE.md",
                "smokes/m223-dirt/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.211.0 M223 Dirt GO");
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
