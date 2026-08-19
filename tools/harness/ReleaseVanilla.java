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
        Properties m162 = load(root, "smokes/m162-wooden-door/smoke.properties");
        Properties m163 = load(root, "smokes/m163-trapdoor/smoke.properties");
        Properties m164 = load(root, "smokes/m164-pressure-plate/smoke.properties");
        Properties m165 = load(root, "smokes/m165-stone-button/smoke.properties");
        match(release, "version", "1.153.0");
        match(release, "milestone", "m165-stone-button");
        same(release, "m162.signature", m162, "expected.signature");
        same(release, "server.sha256", m162, "server.jar.sha256");
        same(release, "m163.signature", m163, "expected.signature");
        same(release, "server.sha256", m163, "server.jar.sha256");
        same(release, "m164.signature", m164, "expected.signature");
        same(release, "server.sha256", m164, "server.jar.sha256");
        same(release, "m165.signature", m165, "expected.signature");
        same(release, "server.sha256", m165, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M162_WOODEN_DOOR.md", "docs/M162_CYCLE.md",
                "smokes/m162-wooden-door/MAP.md", "docs/M163_TRAPDOOR.md", "docs/M163_CYCLE.md",
                "smokes/m163-trapdoor/MAP.md", "docs/M164_PRESSURE_PLATE.md", "docs/M164_CYCLE.md",
                "smokes/m164-pressure-plate/MAP.md", "docs/M165_STONE_BUTTON.md", "docs/M165_CYCLE.md",
                "smokes/m165-stone-button/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.153.0 M165 Stone-button GO");
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
