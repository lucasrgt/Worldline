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
        Properties m409 = load(root, "smokes/m409-spider-string-set/smoke.properties");
        Properties m410 = load(root, "smokes/m410-ghast-fireball-set/smoke.properties");
        Properties m411 = load(root, "smokes/m411-zombie-pigman-set/smoke.properties");
        Properties m412 = load(root, "smokes/m412-slime-split-set/smoke.properties");
        match(release, "version", "1.400.0");
        match(release, "milestone", "m412-slime-split-set");
        same(release, "m409.signature", m409, "expected.signature");
        same(release, "server.sha256", m409, "server.jar.sha256");
        same(release, "m410.signature", m410, "expected.signature");
        same(release, "server.sha256", m410, "server.jar.sha256");
        same(release, "m411.signature", m411, "expected.signature");
        same(release, "server.sha256", m411, "server.jar.sha256");
        same(release, "m412.signature", m412, "expected.signature");
        same(release, "server.sha256", m412, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M409_SPIDER_STRING_SET.md", "docs/M409_CYCLE.md",
                "smokes/m409-spider-string-set/MAP.md", "docs/M410_GHAST_FIREBALL_SET.md", "docs/M410_CYCLE.md",
                "smokes/m410-ghast-fireball-set/MAP.md", "docs/M411_ZOMBIE_PIGMAN_SET.md", "docs/M411_CYCLE.md",
                "smokes/m411-zombie-pigman-set/MAP.md", "docs/M412_SLIME_SPLIT_SET.md", "docs/M412_CYCLE.md",
                "smokes/m412-slime-split-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.400.0 M412 Slime split set GO");
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
