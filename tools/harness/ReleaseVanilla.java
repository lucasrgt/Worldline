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
        Properties m399 = load(root, "smokes/m399-wooden-button-set/smoke.properties");
        Properties m400 = load(root, "smokes/m400-remaining-torch-faces/smoke.properties");
        Properties m401 = load(root, "smokes/m401-remaining-redstone-wire/smoke.properties");
        Properties m402 = load(root, "smokes/m402-remaining-detector-rail/smoke.properties");
        match(release, "version", "1.390.0");
        match(release, "milestone", "m402-remaining-detector-rail");
        same(release, "m399.signature", m399, "expected.signature");
        same(release, "server.sha256", m399, "server.jar.sha256");
        same(release, "m400.signature", m400, "expected.signature");
        same(release, "server.sha256", m400, "server.jar.sha256");
        same(release, "m401.signature", m401, "expected.signature");
        same(release, "server.sha256", m401, "server.jar.sha256");
        same(release, "m402.signature", m402, "expected.signature");
        same(release, "server.sha256", m402, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M399_WOODEN_BUTTON_SET.md", "docs/M399_CYCLE.md",
                "smokes/m399-wooden-button-set/MAP.md", "docs/M400_REMAINING_TORCH_FACES.md", "docs/M400_CYCLE.md",
                "smokes/m400-remaining-torch-faces/MAP.md", "docs/M401_REMAINING_REDSTONE_WIRE.md", "docs/M401_CYCLE.md",
                "smokes/m401-remaining-redstone-wire/MAP.md", "docs/M402_REMAINING_DETECTOR_RAIL.md", "docs/M402_CYCLE.md",
                "smokes/m402-remaining-detector-rail/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.390.0 M402 Remaining detector rail GO");
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
