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
        Properties m333 = load(root, "smokes/m333-dispenser-set/smoke.properties");
        Properties m334 = load(root, "smokes/m334-record-set/smoke.properties");
        Properties m335 = load(root, "smokes/m335-cake-slice-set/smoke.properties");
        Properties m336 = load(root, "smokes/m336-slab-meta-crafts/smoke.properties");
        match(release, "version", "1.324.0");
        match(release, "milestone", "m336-slab-meta-crafts");
        same(release, "m333.signature", m333, "expected.signature");
        same(release, "server.sha256", m333, "server.jar.sha256");
        same(release, "m334.signature", m334, "expected.signature");
        same(release, "server.sha256", m334, "server.jar.sha256");
        same(release, "m335.signature", m335, "expected.signature");
        same(release, "server.sha256", m335, "server.jar.sha256");
        same(release, "m336.signature", m336, "expected.signature");
        same(release, "server.sha256", m336, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M333_DISPENSER_SET.md", "docs/M333_CYCLE.md",
                "smokes/m333-dispenser-set/MAP.md", "docs/M334_RECORD_SET.md", "docs/M334_CYCLE.md",
                "smokes/m334-record-set/MAP.md", "docs/M335_CAKE_SLICE_SET.md", "docs/M335_CYCLE.md",
                "smokes/m335-cake-slice-set/MAP.md", "docs/M336_SLAB_META_CRAFTS.md", "docs/M336_CYCLE.md",
                "smokes/m336-slab-meta-crafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.324.0 M336 Slab meta crafts GO");
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
