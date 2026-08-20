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
        Properties m251 = load(root, "smokes/m251-black-wool/smoke.properties");
        Properties m252 = load(root, "smokes/m252-blue-wool/smoke.properties");
        Properties m253 = load(root, "smokes/m253-green-wool/smoke.properties");
        Properties m254 = load(root, "smokes/m254-water-place/smoke.properties");
        match(release, "version", "1.242.0");
        match(release, "milestone", "m254-water-place");
        same(release, "m251.signature", m251, "expected.signature");
        same(release, "server.sha256", m251, "server.jar.sha256");
        same(release, "m252.signature", m252, "expected.signature");
        same(release, "server.sha256", m252, "server.jar.sha256");
        same(release, "m253.signature", m253, "expected.signature");
        same(release, "server.sha256", m253, "server.jar.sha256");
        same(release, "m254.signature", m254, "expected.signature");
        same(release, "server.sha256", m254, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M251_BLACK_WOOL.md", "docs/M251_CYCLE.md",
                "smokes/m251-black-wool/MAP.md", "docs/M252_BLUE_WOOL.md", "docs/M252_CYCLE.md",
                "smokes/m252-blue-wool/MAP.md", "docs/M253_GREEN_WOOL.md", "docs/M253_CYCLE.md",
                "smokes/m253-green-wool/MAP.md", "docs/M254_WATER_PLACE.md", "docs/M254_CYCLE.md",
                "smokes/m254-water-place/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.242.0 M254 Water place GO");
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
