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
        Properties m412 = load(root, "smokes/m412-slime-split-set/smoke.properties");
        Properties m414 = load(root, "smokes/m414-lava-obsidian-set/smoke.properties");
        Properties m415 = load(root, "smokes/m415-water-cobble-set/smoke.properties");
        Properties m416 = load(root, "smokes/m416-remaining-bookshelf-place/smoke.properties");
        match(release, "version", "1.403.0");
        match(release, "milestone", "m416-remaining-bookshelf-place");
        same(release, "m412.signature", m412, "expected.signature");
        same(release, "server.sha256", m412, "server.jar.sha256");
        same(release, "m414.signature", m414, "expected.signature");
        same(release, "server.sha256", m414, "server.jar.sha256");
        same(release, "m415.signature", m415, "expected.signature");
        same(release, "server.sha256", m415, "server.jar.sha256");
        same(release, "m416.signature", m416, "expected.signature");
        same(release, "server.sha256", m416, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M412_SLIME_SPLIT_SET.md", "docs/M412_CYCLE.md",
                "smokes/m412-slime-split-set/MAP.md", "docs/M414_LAVA_OBSIDIAN_SET.md", "docs/M414_CYCLE.md",
                "smokes/m414-lava-obsidian-set/MAP.md", "docs/M415_WATER_COBBLE_SET.md", "docs/M415_CYCLE.md",
                "smokes/m415-water-cobble-set/MAP.md", "docs/M416_REMAINING_BOOKSHELF_PLACE.md", "docs/M416_CYCLE.md",
                "smokes/m416-remaining-bookshelf-place/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.403.0 M416 Remaining bookshelf place GO");
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
