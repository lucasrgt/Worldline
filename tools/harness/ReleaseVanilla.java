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
        Properties m312 = load(root, "smokes/m312-torch-invert/smoke.properties");
        Properties m313 = load(root, "smokes/m313-note-instruments/smoke.properties");
        Properties m314 = load(root, "smokes/m314-armor-crafts/smoke.properties");
        Properties m315 = load(root, "smokes/m315-dye-wool-crafts/smoke.properties");
        match(release, "version", "1.303.0");
        match(release, "milestone", "m315-dye-wool-crafts");
        same(release, "m312.signature", m312, "expected.signature");
        same(release, "server.sha256", m312, "server.jar.sha256");
        same(release, "m313.signature", m313, "expected.signature");
        same(release, "server.sha256", m313, "server.jar.sha256");
        same(release, "m314.signature", m314, "expected.signature");
        same(release, "server.sha256", m314, "server.jar.sha256");
        same(release, "m315.signature", m315, "expected.signature");
        same(release, "server.sha256", m315, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M312_TORCH_INVERT.md", "docs/M312_CYCLE.md",
                "smokes/m312-torch-invert/MAP.md", "docs/M313_NOTE_INSTRUMENTS.md", "docs/M313_CYCLE.md",
                "smokes/m313-note-instruments/MAP.md", "docs/M314_ARMOR_CRAFTS.md", "docs/M314_CYCLE.md",
                "smokes/m314-armor-crafts/MAP.md", "docs/M315_DYE_WOOL_CRAFTS.md", "docs/M315_CYCLE.md",
                "smokes/m315-dye-wool-crafts/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.303.0 M315 Dye wool crafts GO");
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
