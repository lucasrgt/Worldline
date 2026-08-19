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
        Properties m194 = load(root, "smokes/m194-snow-block/smoke.properties");
        Properties m195 = load(root, "smokes/m195-cobweb/smoke.properties");
        Properties m196 = load(root, "smokes/m196-glass/smoke.properties");
        Properties m197 = load(root, "smokes/m197-wool/smoke.properties");
        match(release, "version", "1.185.0");
        match(release, "milestone", "m197-wool");
        same(release, "m194.signature", m194, "expected.signature");
        same(release, "server.sha256", m194, "server.jar.sha256");
        same(release, "m195.signature", m195, "expected.signature");
        same(release, "server.sha256", m195, "server.jar.sha256");
        same(release, "m196.signature", m196, "expected.signature");
        same(release, "server.sha256", m196, "server.jar.sha256");
        same(release, "m197.signature", m197, "expected.signature");
        same(release, "server.sha256", m197, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M194_SNOW_BLOCK.md", "docs/M194_CYCLE.md",
                "smokes/m194-snow-block/MAP.md", "docs/M195_COBWEB.md", "docs/M195_CYCLE.md",
                "smokes/m195-cobweb/MAP.md", "docs/M196_GLASS.md", "docs/M196_CYCLE.md",
                "smokes/m196-glass/MAP.md", "docs/M197_WOOL.md", "docs/M197_CYCLE.md",
                "smokes/m197-wool/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.185.0 M197 Wool GO");
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
