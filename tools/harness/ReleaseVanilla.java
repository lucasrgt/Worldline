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
        Properties m192 = load(root, "smokes/m192-soul-sand/smoke.properties");
        Properties m193 = load(root, "smokes/m193-ice/smoke.properties");
        Properties m194 = load(root, "smokes/m194-snow-block/smoke.properties");
        Properties m195 = load(root, "smokes/m195-cobweb/smoke.properties");
        match(release, "version", "1.183.0");
        match(release, "milestone", "m195-cobweb");
        same(release, "m192.signature", m192, "expected.signature");
        same(release, "server.sha256", m192, "server.jar.sha256");
        same(release, "m193.signature", m193, "expected.signature");
        same(release, "server.sha256", m193, "server.jar.sha256");
        same(release, "m194.signature", m194, "expected.signature");
        same(release, "server.sha256", m194, "server.jar.sha256");
        same(release, "m195.signature", m195, "expected.signature");
        same(release, "server.sha256", m195, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M192_SOUL_SAND.md", "docs/M192_CYCLE.md",
                "smokes/m192-soul-sand/MAP.md", "docs/M193_ICE.md", "docs/M193_CYCLE.md",
                "smokes/m193-ice/MAP.md", "docs/M194_SNOW_BLOCK.md", "docs/M194_CYCLE.md",
                "smokes/m194-snow-block/MAP.md", "docs/M195_COBWEB.md", "docs/M195_CYCLE.md",
                "smokes/m195-cobweb/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.183.0 M195 Cobweb GO");
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
