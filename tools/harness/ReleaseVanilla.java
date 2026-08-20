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
        Properties m448 = load(root, "smokes/m448-creeper-fuse-set/smoke.properties");
        Properties m452 = load(root, "smokes/m452-knockback-cooldown-set/smoke.properties");
        Properties m457 = load(root, "smokes/m457-spider-leap-set/smoke.properties");
        Properties m458 = load(root, "smokes/m458-slime-touch-set/smoke.properties");
        match(release, "version", "1.441.0");
        match(release, "milestone", "m458-slime-touch-set");
        same(release, "m448.signature", m448, "expected.signature");
        same(release, "server.sha256", m448, "server.jar.sha256");
        same(release, "m452.signature", m452, "expected.signature");
        same(release, "server.sha256", m452, "server.jar.sha256");
        same(release, "m457.signature", m457, "expected.signature");
        same(release, "server.sha256", m457, "server.jar.sha256");
        same(release, "m458.signature", m458, "expected.signature");
        same(release, "server.sha256", m458, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M448_CREEPER_FUSE_SET.md", "docs/M448_CYCLE.md",
                "smokes/m448-creeper-fuse-set/MAP.md", "docs/M452_KNOCKBACK_COOLDOWN_SET.md", "docs/M452_CYCLE.md",
                "smokes/m452-knockback-cooldown-set/MAP.md", "docs/M457_SPIDER_LEAP_SET.md", "docs/M457_CYCLE.md",
                "smokes/m457-spider-leap-set/MAP.md", "docs/M458_SLIME_TOUCH_SET.md", "docs/M458_CYCLE.md",
                "smokes/m458-slime-touch-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.441.0 M458 Slime touch set GO");
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
