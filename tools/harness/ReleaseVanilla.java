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
        Properties m258 = load(root, "smokes/m258-bread-eat/smoke.properties");
        Properties m259 = load(root, "smokes/m259-cooked-pork-eat/smoke.properties");
        Properties m260 = load(root, "smokes/m260-apple-eat/smoke.properties");
        Properties m261 = load(root, "smokes/m261-golden-apple-eat/smoke.properties");
        match(release, "version", "1.249.0");
        match(release, "milestone", "m261-golden-apple-eat");
        same(release, "m258.signature", m258, "expected.signature");
        same(release, "server.sha256", m258, "server.jar.sha256");
        same(release, "m259.signature", m259, "expected.signature");
        same(release, "server.sha256", m259, "server.jar.sha256");
        same(release, "m260.signature", m260, "expected.signature");
        same(release, "server.sha256", m260, "server.jar.sha256");
        same(release, "m261.signature", m261, "expected.signature");
        same(release, "server.sha256", m261, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M258_BREAD_EAT.md", "docs/M258_CYCLE.md",
                "smokes/m258-bread-eat/MAP.md", "docs/M259_COOKED_PORK_EAT.md", "docs/M259_CYCLE.md",
                "smokes/m259-cooked-pork-eat/MAP.md", "docs/M260_APPLE_EAT.md", "docs/M260_CYCLE.md",
                "smokes/m260-apple-eat/MAP.md", "docs/M261_GOLDEN_APPLE_EAT.md", "docs/M261_CYCLE.md",
                "smokes/m261-golden-apple-eat/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.249.0 M261 Golden apple eat GO");
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
