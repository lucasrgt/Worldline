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
        Properties m305 = load(root, "smokes/m305-plant-growth/smoke.properties");
        Properties m306 = load(root, "smokes/m306-closables/smoke.properties");
        Properties m307 = load(root, "smokes/m307-env-damage/smoke.properties");
        Properties m308 = load(root, "smokes/m308-fragile-set/smoke.properties");
        match(release, "version", "1.296.0");
        match(release, "milestone", "m308-fragile-set");
        same(release, "m305.signature", m305, "expected.signature");
        same(release, "server.sha256", m305, "server.jar.sha256");
        same(release, "m306.signature", m306, "expected.signature");
        same(release, "server.sha256", m306, "server.jar.sha256");
        same(release, "m307.signature", m307, "expected.signature");
        same(release, "server.sha256", m307, "server.jar.sha256");
        same(release, "m308.signature", m308, "expected.signature");
        same(release, "server.sha256", m308, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M305_PLANT_GROWTH.md", "docs/M305_CYCLE.md",
                "smokes/m305-plant-growth/MAP.md", "docs/M306_CLOSABLES.md", "docs/M306_CYCLE.md",
                "smokes/m306-closables/MAP.md", "docs/M307_ENV_DAMAGE.md", "docs/M307_CYCLE.md",
                "smokes/m307-env-damage/MAP.md", "docs/M308_FRAGILE_SET.md", "docs/M308_CYCLE.md",
                "smokes/m308-fragile-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.296.0 M308 Fragile set GO");
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
