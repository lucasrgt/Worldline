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
        Properties m196 = load(root, "smokes/m196-glass/smoke.properties");
        Properties m197 = load(root, "smokes/m197-wool/smoke.properties");
        Properties m198 = load(root, "smokes/m198-dandelion/smoke.properties");
        Properties m199 = load(root, "smokes/m199-rose/smoke.properties");
        match(release, "version", "1.187.0");
        match(release, "milestone", "m199-rose");
        same(release, "m196.signature", m196, "expected.signature");
        same(release, "server.sha256", m196, "server.jar.sha256");
        same(release, "m197.signature", m197, "expected.signature");
        same(release, "server.sha256", m197, "server.jar.sha256");
        same(release, "m198.signature", m198, "expected.signature");
        same(release, "server.sha256", m198, "server.jar.sha256");
        same(release, "m199.signature", m199, "expected.signature");
        same(release, "server.sha256", m199, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M196_GLASS.md", "docs/M196_CYCLE.md",
                "smokes/m196-glass/MAP.md", "docs/M197_WOOL.md", "docs/M197_CYCLE.md",
                "smokes/m197-wool/MAP.md", "docs/M198_DANDELION.md", "docs/M198_CYCLE.md",
                "smokes/m198-dandelion/MAP.md", "docs/M199_ROSE.md", "docs/M199_CYCLE.md",
                "smokes/m199-rose/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.187.0 M199 Rose GO");
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
