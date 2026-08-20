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
        Properties m256 = load(root, "smokes/m256-chest-minecart/smoke.properties");
        Properties m257 = load(root, "smokes/m257-furnace-minecart/smoke.properties");
        Properties m258 = load(root, "smokes/m258-bread-eat/smoke.properties");
        Properties m259 = load(root, "smokes/m259-cooked-pork-eat/smoke.properties");
        match(release, "version", "1.247.0");
        match(release, "milestone", "m259-cooked-pork-eat");
        same(release, "m256.signature", m256, "expected.signature");
        same(release, "server.sha256", m256, "server.jar.sha256");
        same(release, "m257.signature", m257, "expected.signature");
        same(release, "server.sha256", m257, "server.jar.sha256");
        same(release, "m258.signature", m258, "expected.signature");
        same(release, "server.sha256", m258, "server.jar.sha256");
        same(release, "m259.signature", m259, "expected.signature");
        same(release, "server.sha256", m259, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M256_CHEST_MINECART.md", "docs/M256_CYCLE.md",
                "smokes/m256-chest-minecart/MAP.md", "docs/M257_FURNACE_MINECART.md", "docs/M257_CYCLE.md",
                "smokes/m257-furnace-minecart/MAP.md", "docs/M258_BREAD_EAT.md", "docs/M258_CYCLE.md",
                "smokes/m258-bread-eat/MAP.md", "docs/M259_COOKED_PORK_EAT.md", "docs/M259_CYCLE.md",
                "smokes/m259-cooked-pork-eat/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.247.0 M259 Cooked pork eat GO");
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
