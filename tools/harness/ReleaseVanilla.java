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
        Properties m386 = load(root, "smokes/m386-ice-snow-melt-set/smoke.properties");
        Properties m387 = load(root, "smokes/m387-remaining-light-set/smoke.properties");
        Properties m388 = load(root, "smokes/m388-hostile-drops-set/smoke.properties");
        Properties m389 = load(root, "smokes/m389-animal-drops-set/smoke.properties");
        match(release, "version", "1.377.0");
        match(release, "milestone", "m389-animal-drops-set");
        same(release, "m386.signature", m386, "expected.signature");
        same(release, "server.sha256", m386, "server.jar.sha256");
        same(release, "m387.signature", m387, "expected.signature");
        same(release, "server.sha256", m387, "server.jar.sha256");
        same(release, "m388.signature", m388, "expected.signature");
        same(release, "server.sha256", m388, "server.jar.sha256");
        same(release, "m389.signature", m389, "expected.signature");
        same(release, "server.sha256", m389, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M386_ICE_SNOW_MELT_SET.md", "docs/M386_CYCLE.md",
                "smokes/m386-ice-snow-melt-set/MAP.md", "docs/M387_REMAINING_LIGHT_SET.md", "docs/M387_CYCLE.md",
                "smokes/m387-remaining-light-set/MAP.md", "docs/M388_HOSTILE_DROPS_SET.md", "docs/M388_CYCLE.md",
                "smokes/m388-hostile-drops-set/MAP.md", "docs/M389_ANIMAL_DROPS_SET.md", "docs/M389_CYCLE.md",
                "smokes/m389-animal-drops-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.377.0 M389 Animal drops set GO");
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
