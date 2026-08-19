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
        Properties m223 = load(root, "smokes/m223-dirt/smoke.properties");
        Properties m224 = load(root, "smokes/m224-netherrack/smoke.properties");
        Properties m225 = load(root, "smokes/m225-coal-ore/smoke.properties");
        Properties m226 = load(root, "smokes/m226-iron-ore/smoke.properties");
        match(release, "version", "1.214.0");
        match(release, "milestone", "m226-iron-ore");
        same(release, "m223.signature", m223, "expected.signature");
        same(release, "server.sha256", m223, "server.jar.sha256");
        same(release, "m224.signature", m224, "expected.signature");
        same(release, "server.sha256", m224, "server.jar.sha256");
        same(release, "m225.signature", m225, "expected.signature");
        same(release, "server.sha256", m225, "server.jar.sha256");
        same(release, "m226.signature", m226, "expected.signature");
        same(release, "server.sha256", m226, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M223_DIRT.md", "docs/M223_CYCLE.md",
                "smokes/m223-dirt/MAP.md", "docs/M224_NETHERRACK.md", "docs/M224_CYCLE.md",
                "smokes/m224-netherrack/MAP.md", "docs/M225_COAL_ORE.md", "docs/M225_CYCLE.md",
                "smokes/m225-coal-ore/MAP.md", "docs/M226_IRON_ORE.md", "docs/M226_CYCLE.md",
                "smokes/m226-iron-ore/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.214.0 M226 Iron ore GO");
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
