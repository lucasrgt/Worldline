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
        Properties m459 = load(root, "smokes/m459-ghast-fireball-hit-set/smoke.properties");
        Properties m460 = load(root, "smokes/m460-monster-bed-interrupt-set/smoke.properties");
        Properties m462 = load(root, "smokes/m462-bow-mob-hit-set/smoke.properties");
        Properties m467 = load(root, "smokes/m467-difficulty-damage-set/smoke.properties");
        match(release, "version", "1.445.0");
        match(release, "milestone", "m467-difficulty-damage-set");
        same(release, "m459.signature", m459, "expected.signature");
        same(release, "server.sha256", m459, "server.jar.sha256");
        same(release, "m460.signature", m460, "expected.signature");
        same(release, "server.sha256", m460, "server.jar.sha256");
        same(release, "m462.signature", m462, "expected.signature");
        same(release, "server.sha256", m462, "server.jar.sha256");
        same(release, "m467.signature", m467, "expected.signature");
        same(release, "server.sha256", m467, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M459_GHAST_FIREBALL_HIT_SET.md", "docs/M459_CYCLE.md",
                "smokes/m459-ghast-fireball-hit-set/MAP.md", "docs/M460_MONSTER_BED_INTERRUPT_SET.md", "docs/M460_CYCLE.md",
                "smokes/m460-monster-bed-interrupt-set/MAP.md", "docs/M462_BOW_MOB_HIT_SET.md", "docs/M462_CYCLE.md",
                "smokes/m462-bow-mob-hit-set/MAP.md", "docs/M467_DIFFICULTY_DAMAGE_SET.md", "docs/M467_CYCLE.md",
                "smokes/m467-difficulty-damage-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.445.0 M467 Difficulty damage set GO");
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
