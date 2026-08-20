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
        Properties m236 = load(root, "smokes/m236-cobble-slab/smoke.properties");
        Properties m237 = load(root, "smokes/m237-stone/smoke.properties");
        Properties m238 = load(root, "smokes/m238-grass/smoke.properties");
        Properties m239 = load(root, "smokes/m239-sand/smoke.properties");
        match(release, "version", "1.227.0");
        match(release, "milestone", "m239-sand");
        same(release, "m236.signature", m236, "expected.signature");
        same(release, "server.sha256", m236, "server.jar.sha256");
        same(release, "m237.signature", m237, "expected.signature");
        same(release, "server.sha256", m237, "server.jar.sha256");
        same(release, "m238.signature", m238, "expected.signature");
        same(release, "server.sha256", m238, "server.jar.sha256");
        same(release, "m239.signature", m239, "expected.signature");
        same(release, "server.sha256", m239, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M236_COBBLE_SLAB.md", "docs/M236_CYCLE.md",
                "smokes/m236-cobble-slab/MAP.md", "docs/M237_STONE.md", "docs/M237_CYCLE.md",
                "smokes/m237-stone/MAP.md", "docs/M238_GRASS.md", "docs/M238_CYCLE.md",
                "smokes/m238-grass/MAP.md", "docs/M239_SAND.md", "docs/M239_CYCLE.md",
                "smokes/m239-sand/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.227.0 M239 Sand GO");
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
