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
        Properties m373 = load(root, "smokes/m373-milk-bucket-set/smoke.properties");
        Properties m374 = load(root, "smokes/m374-remaining-food-eat/smoke.properties");
        Properties m375 = load(root, "smokes/m375-remaining-pick-breaks/smoke.properties");
        Properties m376 = load(root, "smokes/m376-remaining-shovel-breaks/smoke.properties");
        match(release, "version", "1.364.0");
        match(release, "milestone", "m376-remaining-shovel-breaks");
        same(release, "m373.signature", m373, "expected.signature");
        same(release, "server.sha256", m373, "server.jar.sha256");
        same(release, "m374.signature", m374, "expected.signature");
        same(release, "server.sha256", m374, "server.jar.sha256");
        same(release, "m375.signature", m375, "expected.signature");
        same(release, "server.sha256", m375, "server.jar.sha256");
        same(release, "m376.signature", m376, "expected.signature");
        same(release, "server.sha256", m376, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M373_MILK_BUCKET_SET.md", "docs/M373_CYCLE.md",
                "smokes/m373-milk-bucket-set/MAP.md", "docs/M374_REMAINING_FOOD_EAT.md", "docs/M374_CYCLE.md",
                "smokes/m374-remaining-food-eat/MAP.md", "docs/M375_REMAINING_PICK_BREAKS.md", "docs/M375_CYCLE.md",
                "smokes/m375-remaining-pick-breaks/MAP.md", "docs/M376_REMAINING_SHOVEL_BREAKS.md", "docs/M376_CYCLE.md",
                "smokes/m376-remaining-shovel-breaks/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.364.0 M376 Remaining shovel breaks GO");
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
