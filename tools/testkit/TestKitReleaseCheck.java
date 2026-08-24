import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Binds a TestKit release to exact deterministic archive contents. */
public final class TestKitReleaseCheck {
    private static final Pattern VERSION = Pattern.compile(
            "private static final String VERSION = \\\"([^\\\"]+)\\\";");
    private static final String LOCK = "release/testkit-artifacts.lock";
    private TestKitReleaseCheck() { }

    public static void main(String[] arguments) {
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            if (List.of(arguments).equals(List.of("--write-lock"))) { writeLock(root); return; }
            if (arguments.length == 2 && arguments[0].equals("--verify-directory")) {
                validate(root, Path.of(arguments[1]).toAbsolutePath().normalize(), null); return;
            }
            if (arguments.length != 1 || !arguments[0].matches("testkit-v[0-9]+(?:[.][0-9]+){2}"))
                throw new IllegalArgumentException("usage: TestKitReleaseCheck.java "
                        + "testkit-vX.Y.Z|--write-lock|--verify-directory DIR");
            validate(root, root.resolve(".worldline/dist/testkit"), arguments[0]);
        } catch (Exception error) {
            System.err.println("TestKit release check failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void validate(Path root, Path output, String tag) throws Exception {
        Properties release = load(root.resolve("release/testkit.properties"));
        String version = required(release, "version");
        require(tag == null || tag.equals(required(release, "tag")),
                "tag does not match the release manifest");
        require("release-ready".equals(release.getProperty("status")), "TestKit is not release-ready");
        Matcher source = VERSION.matcher(Files.readString(
                root.resolve("tools/testkit/TestKitPackage.java"), StandardCharsets.UTF_8));
        require(source.find() && version.equals(source.group(1)), "TestKit source version drifted");
        Properties lock = load(root.resolve(LOCK));
        require("1".equals(lock.getProperty("schema")) && version.equals(lock.getProperty("version")),
                "TestKit artifact lock version drifted");
        for (String artifact : List.of("api", "runner")) verify(output, lock, artifact);
        require(lock.size() == 12 + integer(lock, "artifact.api.entry.count")
                        + integer(lock, "artifact.runner.entry.count"),
                "unexpected TestKit artifact lock entries");
        try (var paths = Files.list(output)) {
            List<String> jars = paths.filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .map(path -> path.getFileName().toString()).sorted().toList();
            require(jars.equals(List.of(required(lock, "artifact.api.file"),
                    required(lock, "artifact.runner.file")).stream().sorted().toList()),
                    "unexpected TestKit release artifacts: " + jars);
        }
        System.out.println("WORLDLINE_TESTKIT_RELEASE=PASS version=" + version
                + " api.classes=" + required(lock, "artifact.api.class.count")
                + " runner.classes=" + required(lock, "artifact.runner.class.count"));
    }

    private static void verify(Path output, Properties lock, String artifact) throws Exception {
        String prefix = "artifact." + artifact + ".";
        Path path = output.resolve(required(lock, prefix + "file"));
        require(Files.size(path) == Long.parseLong(required(lock, prefix + "bytes")),
                "artifact size drift: " + path.getFileName());
        require(digest(path).equals(required(lock, prefix + "sha256")),
                "artifact checksum drift: " + path.getFileName());
        try (JarFile archive = new JarFile(path.toFile())) {
            var entries = archive.stream().filter(entry -> !entry.isDirectory()).sorted(
                    java.util.Comparator.comparing(java.util.jar.JarEntry::getName)).toList();
            require(entries.size() == integer(lock, prefix + "entry.count"),
                    "artifact entry count drift: " + path.getFileName());
            long classes = entries.stream().filter(entry -> entry.getName().endsWith(".class")).count();
            require(classes == integer(lock, prefix + "class.count"),
                    "artifact class count drift: " + path.getFileName());
            for (var entry : entries) try (var input = archive.getInputStream(entry)) {
                require(digest(input.readAllBytes()).equals(required(lock,
                                prefix + "entry." + entry.getName())),
                        "artifact entry drift: " + entry.getName());
            }
            String main = archive.getManifest().getMainAttributes().getValue("Main-Class");
            require(artifact.equals("runner") == "worldline.cli.WorldlineCli".equals(main),
                    "artifact entry point drift: " + path.getFileName());
        }
    }

    private static void writeLock(Path root) throws Exception {
        String version = required(load(root.resolve("release/testkit.properties")), "version");
        Path output = root.resolve(".worldline/dist/testkit");
        TreeMap<String, String> rows = new TreeMap<>();
        rows.put("schema", "1"); rows.put("version", version);
        capture(output.resolve("worldline-test-api-" + version + ".jar"), rows, "api");
        capture(output.resolve("worldline-test-runner-" + version + ".jar"), rows, "runner");
        StringBuilder text = new StringBuilder("# Exact deterministic Worldline TestKit artifacts\n");
        rows.forEach((key, value) -> text.append(key).append('=').append(value).append('\n'));
        Files.writeString(root.resolve(LOCK), text, StandardCharsets.UTF_8);
        System.out.println("WORLDLINE_TESTKIT_ARTIFACT_LOCK=WRITTEN");
    }

    private static void capture(Path path, TreeMap<String, String> rows, String artifact)
            throws Exception {
        String prefix = "artifact." + artifact + ".";
        rows.put(prefix + "file", path.getFileName().toString());
        rows.put(prefix + "bytes", Long.toString(Files.size(path)));
        rows.put(prefix + "sha256", digest(path));
        try (JarFile archive = new JarFile(path.toFile())) {
            var entries = archive.stream().filter(entry -> !entry.isDirectory()).sorted(
                    java.util.Comparator.comparing(java.util.jar.JarEntry::getName)).toList();
            rows.put(prefix + "entry.count", Integer.toString(entries.size()));
            rows.put(prefix + "class.count", Long.toString(entries.stream()
                    .filter(entry -> entry.getName().endsWith(".class")).count()));
            for (var entry : entries) try (var input = archive.getInputStream(entry)) {
                rows.put(prefix + "entry." + entry.getName(), digest(input.readAllBytes()));
            }
        }
    }

    private static Properties load(Path path) throws IOException {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static String digest(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing release artifact: " + path.getFileName());
        return digest(Files.readAllBytes(path));
    }
    private static String digest(byte[] value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value)); }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(),
                "missing " + key); return value.trim();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
