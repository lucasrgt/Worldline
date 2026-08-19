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
        Properties m215 = load(root, "smokes/m215-lapis-block/smoke.properties");
        Properties m216 = load(root, "smokes/m216-obsidian/smoke.properties");
        Properties m217 = load(root, "smokes/m217-mossy-cobble/smoke.properties");
        Properties m218 = load(root, "smokes/m218-gravel/smoke.properties");
        match(release, "version", "1.206.0");
        match(release, "milestone", "m218-gravel");
        same(release, "m215.signature", m215, "expected.signature");
        same(release, "server.sha256", m215, "server.jar.sha256");
        same(release, "m216.signature", m216, "expected.signature");
        same(release, "server.sha256", m216, "server.jar.sha256");
        same(release, "m217.signature", m217, "expected.signature");
        same(release, "server.sha256", m217, "server.jar.sha256");
        same(release, "m218.signature", m218, "expected.signature");
        same(release, "server.sha256", m218, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M215_LAPIS_BLOCK.md", "docs/M215_CYCLE.md",
                "smokes/m215-lapis-block/MAP.md", "docs/M216_OBSIDIAN.md", "docs/M216_CYCLE.md",
                "smokes/m216-obsidian/MAP.md", "docs/M217_MOSSY_COBBLE.md", "docs/M217_CYCLE.md",
                "smokes/m217-mossy-cobble/MAP.md", "docs/M218_GRAVEL.md", "docs/M218_CYCLE.md",
                "smokes/m218-gravel/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.206.0 M218 Gravel GO");
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
