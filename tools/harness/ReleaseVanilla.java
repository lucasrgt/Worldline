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
        Properties m260 = load(root, "smokes/m260-apple-eat/smoke.properties");
        Properties m261 = load(root, "smokes/m261-golden-apple-eat/smoke.properties");
        Properties m262 = load(root, "smokes/m262-cookie-eat/smoke.properties");
        Properties m263 = load(root, "smokes/m263-stew-eat/smoke.properties");
        match(release, "version", "1.251.0");
        match(release, "milestone", "m263-stew-eat");
        same(release, "m260.signature", m260, "expected.signature");
        same(release, "server.sha256", m260, "server.jar.sha256");
        same(release, "m261.signature", m261, "expected.signature");
        same(release, "server.sha256", m261, "server.jar.sha256");
        same(release, "m262.signature", m262, "expected.signature");
        same(release, "server.sha256", m262, "server.jar.sha256");
        same(release, "m263.signature", m263, "expected.signature");
        same(release, "server.sha256", m263, "server.jar.sha256");
        for (String file : Arrays.asList("docs/M260_APPLE_EAT.md", "docs/M260_CYCLE.md",
                "smokes/m260-apple-eat/MAP.md", "docs/M261_GOLDEN_APPLE_EAT.md", "docs/M261_CYCLE.md",
                "smokes/m261-golden-apple-eat/MAP.md", "docs/M262_COOKIE_EAT.md", "docs/M262_CYCLE.md",
                "smokes/m262-cookie-eat/MAP.md", "docs/M263_STEW_EAT.md", "docs/M263_CYCLE.md",
                "smokes/m263-stew-eat/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        System.out.println("  release: Worldline v1.251.0 M263 Stew eat GO");
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
