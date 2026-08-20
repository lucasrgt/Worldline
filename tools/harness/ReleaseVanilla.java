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
        Properties m249 = load(root, "smokes/m249-yellow-wool/smoke.properties");
        Properties m250 = load(root, "smokes/m250-red-wool/smoke.properties");
        Properties m251 = load(root, "smokes/m251-black-wool/smoke.properties");
        Properties m252 = load(root, "smokes/m252-blue-wool/smoke.properties");
        match(release, "version", "1.240.0");
        match(release, "milestone", "m252-blue-wool");
        same(release, "m249.signature", m249, "expected.signature");
        same(release, "server.sha256", m249, "server.jar.sha256");
        same(release, "m250.signature", m250, "expected.signature");
        same(release, "server.sha256", m250, "server.jar.sha256");
        same(release, "m251.signature", m251, "expected.signature");
        same(release, "server.sha256", m251, "server.jar.sha256");
        same(release, "m252.signature", m252, "expected.signature");
        same(release, "server.sha256", m252, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M249_YELLOW_WOOL.md", "docs/M249_CYCLE.md",
                "smokes/m249-yellow-wool/MAP.md", "docs/M250_RED_WOOL.md", "docs/M250_CYCLE.md",
                "smokes/m250-red-wool/MAP.md", "docs/M251_BLACK_WOOL.md", "docs/M251_CYCLE.md",
                "smokes/m251-black-wool/MAP.md", "docs/M252_BLUE_WOOL.md", "docs/M252_CYCLE.md",
                "smokes/m252-blue-wool/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.240.0 M252 Blue wool GO");
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
