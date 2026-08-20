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
        Properties m444 = load(root, "smokes/m444-remaining-mob-drops-rest/smoke.properties");
        Properties m422 = load(root, "smokes/m422-skeleton-bone-set/smoke.properties");
        Properties m436 = load(root, "smokes/m436-remaining-arrow-life-set/smoke.properties");
        Properties m445 = load(root, "smokes/m445-skeleton-ranged-ai-set/smoke.properties");
        match(release, "version", "1.433.0");
        match(release, "milestone", "m445-skeleton-ranged-ai-set");
        same(release, "m444.signature", m444, "expected.signature");
        same(release, "server.sha256", m444, "server.jar.sha256");
        same(release, "m422.signature", m422, "expected.signature");
        same(release, "server.sha256", m422, "server.jar.sha256");
        same(release, "m436.signature", m436, "expected.signature");
        same(release, "server.sha256", m436, "server.jar.sha256");
        same(release, "m445.signature", m445, "expected.signature");
        same(release, "server.sha256", m445, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M444_REMAINING_MOB_DROPS_REST.md", "docs/M444_CYCLE.md",
                "smokes/m444-remaining-mob-drops-rest/MAP.md", "docs/M422_SKELETON_BONE_SET.md", "docs/M422_CYCLE.md",
                "smokes/m422-skeleton-bone-set/MAP.md", "docs/M436_REMAINING_ARROW_LIFE_SET.md", "docs/M436_CYCLE.md",
                "smokes/m436-remaining-arrow-life-set/MAP.md", "docs/M445_SKELETON_RANGED_AI_SET.md", "docs/M445_CYCLE.md",
                "smokes/m445-skeleton-ranged-ai-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.433.0 M445 Skeleton ranged AI set GO");
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
