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
        Properties m350 = load(root, "smokes/m350-sign-text-set/smoke.properties");
        Properties m351 = load(root, "smokes/m351-painting-orient-set/smoke.properties");
        Properties m352 = load(root, "smokes/m352-tool-durability-set/smoke.properties");
        Properties m353 = load(root, "smokes/m353-sword-damage-set/smoke.properties");
        match(release, "version", "1.341.0");
        match(release, "milestone", "m353-sword-damage-set");
        same(release, "m350.signature", m350, "expected.signature");
        same(release, "server.sha256", m350, "server.jar.sha256");
        same(release, "m351.signature", m351, "expected.signature");
        same(release, "server.sha256", m351, "server.jar.sha256");
        same(release, "m352.signature", m352, "expected.signature");
        same(release, "server.sha256", m352, "server.jar.sha256");
        same(release, "m353.signature", m353, "expected.signature");
        same(release, "server.sha256", m353, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M350_SIGN_TEXT_SET.md", "docs/M350_CYCLE.md",
                "smokes/m350-sign-text-set/MAP.md", "docs/M351_PAINTING_ORIENT_SET.md", "docs/M351_CYCLE.md",
                "smokes/m351-painting-orient-set/MAP.md", "docs/M352_TOOL_DURABILITY_SET.md", "docs/M352_CYCLE.md",
                "smokes/m352-tool-durability-set/MAP.md", "docs/M353_SWORD_DAMAGE_SET.md", "docs/M353_CYCLE.md",
                "smokes/m353-sword-damage-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.341.0 M353 Sword damage set GO");
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
