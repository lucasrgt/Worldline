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
        Properties m469 = load(root, "smokes/m469-void-death-set/smoke.properties");
        Properties m554 = load(root, "smokes/m554-extended-head-break-set/smoke.properties");
        Properties m546 = load(root, "smokes/m546-piston-qc-set/smoke.properties");
        Properties m547 = load(root, "smokes/m547-sticky-piston-qc-set/smoke.properties");
        match(release, "version", "1.449.0");
        match(release, "milestone", "m547-sticky-piston-qc-set");
        same(release, "m469.signature", m469, "expected.signature");
        same(release, "server.sha256", m469, "server.jar.sha256");
        same(release, "m554.signature", m554, "expected.signature");
        same(release, "server.sha256", m554, "server.jar.sha256");
        same(release, "m546.signature", m546, "expected.signature");
        same(release, "server.sha256", m546, "server.jar.sha256");
        same(release, "m547.signature", m547, "expected.signature");
        same(release, "server.sha256", m547, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M469_VOID_DEATH_SET.md", "docs/M469_CYCLE.md",
                "smokes/m469-void-death-set/MAP.md", "docs/M554_EXTENDED_HEAD_BREAK_SET.md", "docs/M554_CYCLE.md",
                "smokes/m554-extended-head-break-set/MAP.md", "docs/M546_PISTON_QC_SET.md", "docs/M546_CYCLE.md",
                "smokes/m546-piston-qc-set/MAP.md", "docs/M547_STICKY_PISTON_QC_SET.md", "docs/M547_CYCLE.md",
                "smokes/m547-sticky-piston-qc-set/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.449.0 M547 Sticky piston QC set GO");
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
