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
        Properties m439 = load(root, "smokes/m439-remaining-ore-place-set/smoke.properties");
        Properties m440 = load(root, "smokes/m440-remaining-dye-rest-set/smoke.properties");
        Properties m442 = load(root, "smokes/m442-remaining-record-place-set/smoke.properties");
        Properties m443 = load(root, "smokes/m443-remaining-bucket-rest-set/smoke.properties");
        match(release, "version", "1.429.0");
        match(release, "milestone", "m443-remaining-bucket-rest-set");
        same(release, "m439.signature", m439, "expected.signature");
        same(release, "server.sha256", m439, "server.jar.sha256");
        same(release, "m440.signature", m440, "expected.signature");
        same(release, "server.sha256", m440, "server.jar.sha256");
        same(release, "m442.signature", m442, "expected.signature");
        same(release, "server.sha256", m442, "server.jar.sha256");
        same(release, "m443.signature", m443, "expected.signature");
        same(release, "server.sha256", m443, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M439_REMAINING_ORE_PLACE_SET.md", "docs/M439_CYCLE.md",
                "smokes/m439-remaining-ore-place-set/MAP.md", "docs/M440_REMAINING_DYE_REST_SET.md", "docs/M440_CYCLE.md",
                "smokes/m440-remaining-dye-rest-set/MAP.md", "docs/M442_REMAINING_RECORD_PLACE_SET.md", "docs/M442_CYCLE.md",
                "smokes/m442-remaining-record-place-set/MAP.md", "docs/M443_REMAINING_BUCKET_REST_SET.md", "docs/M443_CYCLE.md",
                "smokes/m443-remaining-bucket-rest-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.429.0 M443 Remaining bucket rest set GO");
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
