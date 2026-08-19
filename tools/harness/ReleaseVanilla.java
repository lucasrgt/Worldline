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
        Properties m178 = load(root, "smokes/m178-jukebox/smoke.properties");
        Properties m179 = load(root, "smokes/m179-wheat/smoke.properties");
        Properties m180 = load(root, "smokes/m180-fishing-rod/smoke.properties");
        Properties m181 = load(root, "smokes/m181-lava-bucket/smoke.properties");
        match(release, "version", "1.169.0");
        match(release, "milestone", "m181-lava-bucket");
        same(release, "m178.signature", m178, "expected.signature");
        same(release, "server.sha256", m178, "server.jar.sha256");
        same(release, "m179.signature", m179, "expected.signature");
        same(release, "server.sha256", m179, "server.jar.sha256");
        same(release, "m180.signature", m180, "expected.signature");
        same(release, "server.sha256", m180, "server.jar.sha256");
        same(release, "m181.signature", m181, "expected.signature");
        same(release, "server.sha256", m181, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M178_JUKEBOX.md", "docs/M178_CYCLE.md",
                "smokes/m178-jukebox/MAP.md", "docs/M179_WHEAT.md", "docs/M179_CYCLE.md",
                "smokes/m179-wheat/MAP.md", "docs/M180_FISHING_ROD.md", "docs/M180_CYCLE.md",
                "smokes/m180-fishing-rod/MAP.md", "docs/M181_LAVA_BUCKET.md", "docs/M181_CYCLE.md",
                "smokes/m181-lava-bucket/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.169.0 M181 Lava bucket GO");
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
