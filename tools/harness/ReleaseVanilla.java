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
        Properties m553 = load(root, "smokes/m553-piston-immovable-set/smoke.properties");
        Properties m549 = load(root, "smokes/m549-sticky-bud-set/smoke.properties");
        Properties m559 = load(root, "smokes/m559-double-extender-set/smoke.properties");
        Properties m560 = load(root, "smokes/m560-portal-scale-set/smoke.properties");
        match(release, "version", "1.456.0");
        match(release, "milestone", "m560-portal-scale-set");
        same(release, "m553.signature", m553, "expected.signature");
        same(release, "server.sha256", m553, "server.jar.sha256");
        same(release, "m549.signature", m549, "expected.signature");
        same(release, "server.sha256", m549, "server.jar.sha256");
        same(release, "m559.signature", m559, "expected.signature");
        same(release, "server.sha256", m559, "server.jar.sha256");
        same(release, "m560.signature", m560, "expected.signature");
        same(release, "server.sha256", m560, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M553_PISTON_IMMOVABLE_SET.md", "docs/M553_CYCLE.md",
                "smokes/m553-piston-immovable-set/MAP.md", "docs/M549_STICKY_BUD_SET.md", "docs/M549_CYCLE.md",
                "smokes/m549-sticky-bud-set/MAP.md", "docs/M559_DOUBLE_EXTENDER_SET.md", "docs/M559_CYCLE.md",
                "smokes/m559-double-extender-set/MAP.md", "docs/M560_PORTAL_SCALE_SET.md", "docs/M560_CYCLE.md",
                "smokes/m560-portal-scale-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.456.0 M560 Portal scale set GO");
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
