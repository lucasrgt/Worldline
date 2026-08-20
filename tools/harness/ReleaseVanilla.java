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
        Properties m309 = load(root, "smokes/m309-rail-power/smoke.properties");
        Properties m310 = load(root, "smokes/m310-vehicle-rides/smoke.properties");
        Properties m311 = load(root, "smokes/m311-storage-carts/smoke.properties");
        Properties m312 = load(root, "smokes/m312-torch-invert/smoke.properties");
        match(release, "version", "1.300.0");
        match(release, "milestone", "m312-torch-invert");
        same(release, "m309.signature", m309, "expected.signature");
        same(release, "server.sha256", m309, "server.jar.sha256");
        same(release, "m310.signature", m310, "expected.signature");
        same(release, "server.sha256", m310, "server.jar.sha256");
        same(release, "m311.signature", m311, "expected.signature");
        same(release, "server.sha256", m311, "server.jar.sha256");
        same(release, "m312.signature", m312, "expected.signature");
        same(release, "server.sha256", m312, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M309_RAIL_POWER.md", "docs/M309_CYCLE.md",
                "smokes/m309-rail-power/MAP.md", "docs/M310_VEHICLE_RIDES.md", "docs/M310_CYCLE.md",
                "smokes/m310-vehicle-rides/MAP.md", "docs/M311_STORAGE_CARTS.md", "docs/M311_CYCLE.md",
                "smokes/m311-storage-carts/MAP.md", "docs/M312_TORCH_INVERT.md", "docs/M312_CYCLE.md",
                "smokes/m312-torch-invert/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.300.0 M312 Torch invert GO");
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
