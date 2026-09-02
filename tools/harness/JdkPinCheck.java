import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Fails closed when CI or harness JDK pins leave the documented closed set. */
final class JdkPinCheck {
    private static final Pattern JAVA_VERSION = Pattern.compile(
            "java-version:\\s*'([^']+)'");

    private JdkPinCheck() { }

    static void execute(Path root) throws Exception {
        Properties pins = StrictProperties.load(root.resolve("quality/jdk-pins.properties"));
        require("1".equals(pins.getProperty("schema")), "invalid JDK pin schema");
        Properties harness = StrictProperties.load(root.resolve("harness.properties"));
        Properties release = StrictProperties.load(root.resolve("release/worldline.properties"));
        require(required(pins, "product.release").equals(harness.getProperty("java.release")),
                "harness java.release is outside the closed JDK set");
        require(required(pins, "test.release").equals(harness.getProperty("test.release")),
                "harness test.release is outside the closed JDK set");
        require(required(pins, "product.release").equals(release.getProperty("java.release")),
                "release java.release is outside the closed JDK set");
        Set<String> allowed = new LinkedHashSet<>();
        allowed.add(required(pins, "ci.temurin"));
        allowed.add(required(pins, "consumer.java.21"));
        allowed.add(required(pins, "consumer.java.25"));
        Set<String> observed = new LinkedHashSet<>();
        try (Stream<Path> paths = Files.walk(root.resolve(".github"))) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String name = path.getFileName().toString();
                if (!name.endsWith(".yml") && !name.endsWith(".yaml")) continue;
                String text = Files.readString(path, StandardCharsets.UTF_8);
                Matcher matcher = JAVA_VERSION.matcher(text);
                while (matcher.find()) observed.add(matcher.group(1));
            }
        }
        for (String version : observed)
            require(allowed.contains(version),
                    "undocumented CI JDK pin: " + version);
        String consumer = Files.readString(
                root.resolve(".github/workflows/testkit-external-consumer.yml"),
                StandardCharsets.UTF_8);
        require(consumer.contains("java: ['21', '25']"),
                "consumer JDK matrix drifted from the closed set");
        require(!consumer.contains("Acquire.java client")
                        && !consumer.contains("minecraft-b1.7.3-client.jar"),
                "external-consumer workflow downloads the official client JAR");
        String plugin = Files.readString(
                root.resolve("tooling/gradle-plugin/build.gradle.kts"), StandardCharsets.UTF_8);
        require(plugin.contains("JavaLanguageVersion.of(" + required(pins, "gradle.plugin.release")
                + ")"), "Gradle plugin JDK pin drifted from the closed set");
        System.out.println("  JDK pins: closed set product=" + required(pins, "product.release")
                + " test=" + required(pins, "test.release")
                + " ci=" + required(pins, "ci.temurin"));
    }

    static void selfTest() throws Exception {
        execute(Path.of("").toAbsolutePath().normalize());
        System.out.println("  JDK pin self-test: passed");
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing " + key);
        return value.trim();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
