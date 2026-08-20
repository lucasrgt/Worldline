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
        Properties m354 = load(root, "smokes/m354-farmland-hydrate-set/smoke.properties");
        Properties m355 = load(root, "smokes/m355-note-rest-instruments/smoke.properties");
        Properties m356 = load(root, "smokes/m356-jack-o-lantern-crafts/smoke.properties");
        Properties m357 = load(root, "smokes/m357-glowstone-dust-crafts/smoke.properties");
        match(release, "version", "1.345.0");
        match(release, "milestone", "m357-glowstone-dust-crafts");
        same(release, "m354.signature", m354, "expected.signature");
        same(release, "server.sha256", m354, "server.jar.sha256");
        same(release, "m355.signature", m355, "expected.signature");
        same(release, "server.sha256", m355, "server.jar.sha256");
        same(release, "m356.signature", m356, "expected.signature");
        same(release, "server.sha256", m356, "server.jar.sha256");
        same(release, "m357.signature", m357, "expected.signature");
        same(release, "server.sha256", m357, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M354_FARMLAND_HYDRATE_SET.md", "docs/M354_CYCLE.md",
                "smokes/m354-farmland-hydrate-set/MAP.md", "docs/M355_NOTE_REST_INSTRUMENTS.md", "docs/M355_CYCLE.md",
                "smokes/m355-note-rest-instruments/MAP.md", "docs/M356_JACK_O_LANTERN_CRAFTS.md", "docs/M356_CYCLE.md",
                "smokes/m356-jack-o-lantern-crafts/MAP.md", "docs/M357_GLOWSTONE_DUST_CRAFTS.md", "docs/M357_CYCLE.md",
                "smokes/m357-glowstone-dust-crafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.345.0 M357 Glowstone dust crafts GO");
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
