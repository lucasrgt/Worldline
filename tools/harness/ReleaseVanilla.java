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
        Properties m416 = load(root, "smokes/m416-remaining-bookshelf-place/smoke.properties");
        Properties m417 = load(root, "smokes/m417-remaining-tnt-place/smoke.properties");
        Properties m418 = load(root, "smokes/m418-remaining-obsidian-place/smoke.properties");
        Properties m419 = load(root, "smokes/m419-remaining-netherrack-place/smoke.properties");
        match(release, "version", "1.406.0");
        match(release, "milestone", "m419-remaining-netherrack-place");
        same(release, "m416.signature", m416, "expected.signature");
        same(release, "server.sha256", m416, "server.jar.sha256");
        same(release, "m417.signature", m417, "expected.signature");
        same(release, "server.sha256", m417, "server.jar.sha256");
        same(release, "m418.signature", m418, "expected.signature");
        same(release, "server.sha256", m418, "server.jar.sha256");
        same(release, "m419.signature", m419, "expected.signature");
        same(release, "server.sha256", m419, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M416_REMAINING_BOOKSHELF_PLACE.md", "docs/M416_CYCLE.md",
                "smokes/m416-remaining-bookshelf-place/MAP.md", "docs/M417_REMAINING_TNT_PLACE.md", "docs/M417_CYCLE.md",
                "smokes/m417-remaining-tnt-place/MAP.md", "docs/M418_REMAINING_OBSIDIAN_PLACE.md", "docs/M418_CYCLE.md",
                "smokes/m418-remaining-obsidian-place/MAP.md", "docs/M419_REMAINING_NETHERRACK_PLACE.md", "docs/M419_CYCLE.md",
                "smokes/m419-remaining-netherrack-place/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.406.0 M419 Remaining netherrack place GO");
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
