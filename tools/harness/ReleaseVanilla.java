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
        Properties m153 = load(root, "smokes/m153-dispenser-eject/smoke.properties");
        Properties m154 = load(root, "smokes/m154-boat-spawn/smoke.properties");
        Properties m155 = load(root, "smokes/m155-minecart-spawn/smoke.properties");
        Properties m156 = load(root, "smokes/m156-farmland-hydration/smoke.properties");
        match(release, "version", "1.144.0");
        match(release, "milestone", "m156-farmland-hydration");
        same(release, "m153.signature", m153, "expected.signature");
        same(release, "server.sha256", m153, "server.jar.sha256");
        same(release, "m154.signature", m154, "expected.signature");
        same(release, "server.sha256", m154, "server.jar.sha256");
        same(release, "m155.signature", m155, "expected.signature");
        same(release, "server.sha256", m155, "server.jar.sha256");
        same(release, "m156.signature", m156, "expected.signature");
        same(release, "server.sha256", m156, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M153_DISPENSER_EJECT.md", "docs/M153_CYCLE.md",
                "smokes/m153-dispenser-eject/MAP.md", "docs/M154_BOAT_SPAWN.md", "docs/M154_CYCLE.md",
                "smokes/m154-boat-spawn/MAP.md", "docs/M155_MINECART_SPAWN.md", "docs/M155_CYCLE.md",
                "smokes/m155-minecart-spawn/MAP.md", "docs/M156_FARMLAND_HYDRATION.md", "docs/M156_CYCLE.md",
                "smokes/m156-farmland-hydration/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.144.0 M156 Farmland-hydration GO");
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
