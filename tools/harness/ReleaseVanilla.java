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
        Properties m462 = load(root, "smokes/m462-bow-mob-hit-set/smoke.properties");
        Properties m467 = load(root, "smokes/m467-difficulty-damage-set/smoke.properties");
        Properties m469 = load(root, "smokes/m469-void-death-set/smoke.properties");
        Properties m554 = load(root, "smokes/m554-extended-head-break-set/smoke.properties");
        match(release, "version", "1.447.0");
        match(release, "milestone", "m554-extended-head-break-set");
        same(release, "m462.signature", m462, "expected.signature");
        same(release, "server.sha256", m462, "server.jar.sha256");
        same(release, "m467.signature", m467, "expected.signature");
        same(release, "server.sha256", m467, "server.jar.sha256");
        same(release, "m469.signature", m469, "expected.signature");
        same(release, "server.sha256", m469, "server.jar.sha256");
        same(release, "m554.signature", m554, "expected.signature");
        same(release, "server.sha256", m554, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M462_BOW_MOB_HIT_SET.md", "docs/M462_CYCLE.md",
                "smokes/m462-bow-mob-hit-set/MAP.md", "docs/M467_DIFFICULTY_DAMAGE_SET.md", "docs/M467_CYCLE.md",
                "smokes/m467-difficulty-damage-set/MAP.md", "docs/M469_VOID_DEATH_SET.md", "docs/M469_CYCLE.md",
                "smokes/m469-void-death-set/MAP.md", "docs/M554_EXTENDED_HEAD_BREAK_SET.md", "docs/M554_CYCLE.md",
                "smokes/m554-extended-head-break-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.447.0 M554 Extended head break set GO");
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
