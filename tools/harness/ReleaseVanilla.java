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
        Properties m190 = load(root, "smokes/m190-jack-o-lantern/smoke.properties");
        Properties m191 = load(root, "smokes/m191-glowstone/smoke.properties");
        Properties m192 = load(root, "smokes/m192-soul-sand/smoke.properties");
        Properties m193 = load(root, "smokes/m193-ice/smoke.properties");
        match(release, "version", "1.181.0");
        match(release, "milestone", "m193-ice");
        same(release, "m190.signature", m190, "expected.signature");
        same(release, "server.sha256", m190, "server.jar.sha256");
        same(release, "m191.signature", m191, "expected.signature");
        same(release, "server.sha256", m191, "server.jar.sha256");
        same(release, "m192.signature", m192, "expected.signature");
        same(release, "server.sha256", m192, "server.jar.sha256");
        same(release, "m193.signature", m193, "expected.signature");
        same(release, "server.sha256", m193, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M190_JACK_O_LANTERN.md", "docs/M190_CYCLE.md",
                "smokes/m190-jack-o-lantern/MAP.md", "docs/M191_GLOWSTONE.md", "docs/M191_CYCLE.md",
                "smokes/m191-glowstone/MAP.md", "docs/M192_SOUL_SAND.md", "docs/M192_CYCLE.md",
                "smokes/m192-soul-sand/MAP.md", "docs/M193_ICE.md", "docs/M193_CYCLE.md",
                "smokes/m193-ice/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.181.0 M193 Ice GO");
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
