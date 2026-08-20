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
        Properties m382 = load(root, "smokes/m382-portal-obsidian-set/smoke.properties");
        Properties m383 = load(root, "smokes/m383-mushroom-place-set/smoke.properties");
        Properties m384 = load(root, "smokes/m384-cactus-sugar-set/smoke.properties");
        Properties m385 = load(root, "smokes/m385-leaf-decay-set/smoke.properties");
        match(release, "version", "1.373.0");
        match(release, "milestone", "m385-leaf-decay-set");
        same(release, "m382.signature", m382, "expected.signature");
        same(release, "server.sha256", m382, "server.jar.sha256");
        same(release, "m383.signature", m383, "expected.signature");
        same(release, "server.sha256", m383, "server.jar.sha256");
        same(release, "m384.signature", m384, "expected.signature");
        same(release, "server.sha256", m384, "server.jar.sha256");
        same(release, "m385.signature", m385, "expected.signature");
        same(release, "server.sha256", m385, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M382_PORTAL_OBSIDIAN_SET.md", "docs/M382_CYCLE.md",
                "smokes/m382-portal-obsidian-set/MAP.md", "docs/M383_MUSHROOM_PLACE_SET.md", "docs/M383_CYCLE.md",
                "smokes/m383-mushroom-place-set/MAP.md", "docs/M384_CACTUS_SUGAR_SET.md", "docs/M384_CYCLE.md",
                "smokes/m384-cactus-sugar-set/MAP.md", "docs/M385_LEAF_DECAY_SET.md", "docs/M385_CYCLE.md",
                "smokes/m385-leaf-decay-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.373.0 M385 Leaf decay set GO");
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
