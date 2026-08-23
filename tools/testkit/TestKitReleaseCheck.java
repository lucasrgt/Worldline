import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Binds a TestKit release tag to generated artifacts and their generated checksums. */
public final class TestKitReleaseCheck {
    private static final Pattern VERSION = Pattern.compile(
            "private static final String VERSION = \\\"([^\\\"]+)\\\";");

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 1 || !arguments[0].matches("testkit-v[0-9]+(?:[.][0-9]+){2}"))
                throw new IllegalArgumentException("usage: TestKitReleaseCheck.java testkit-vX.Y.Z");
            Path root = Path.of("").toAbsolutePath().normalize();
            String version = arguments[0].substring("testkit-v".length());
            Matcher source = VERSION.matcher(Files.readString(
                    root.resolve("tools/testkit/TestKitPackage.java"), StandardCharsets.UTF_8));
            require(source.find() && version.equals(source.group(1)), "tag does not match TestKit version");
            Path output = root.resolve(".worldline/dist/testkit");
            Properties checksums = new Properties();
            try (Reader reader = Files.newBufferedReader(output.resolve("checksums.properties"),
                    StandardCharsets.US_ASCII)) { checksums.load(reader); }
            require("1".equals(checksums.getProperty("format")), "checksum format drifted");
            for (String artifact : new String[] {"worldline-test-api", "worldline-test-runner"}) {
                String name = artifact + "-" + version + ".jar";
                String expected = checksums.getProperty(name, "");
                require(expected.matches("[0-9a-f]{64}"), "missing generated checksum for " + name);
                require(expected.equals(digest(output.resolve(name))), "artifact checksum drift: " + name);
            }
            require(checksums.size() == 3, "unexpected TestKit checksum entries");
            try (var paths = Files.list(output)) {
                List<String> jars = paths.filter(path -> path.getFileName().toString().endsWith(".jar"))
                        .map(path -> path.getFileName().toString()).sorted().toList();
                require(jars.equals(List.of("worldline-test-api-" + version + ".jar",
                        "worldline-test-runner-" + version + ".jar")),
                        "unexpected TestKit release artifacts: " + jars);
            }
            System.out.println("WORLDLINE_TESTKIT_RELEASE=PASS version=" + version);
        } catch (Exception error) {
            System.err.println("TestKit release check failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static String digest(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing release artifact: " + path.getFileName());
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
