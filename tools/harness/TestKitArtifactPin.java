import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

/** Seals exact deterministic TestKit package bytes after an exact static build. */
final class TestKitArtifactPin {
    private static final List<String> ARTIFACTS = List.of("api", "runner");
    private TestKitArtifactPin() { }

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--write")),
                    "usage: TestKitArtifactPin --write");
            Path root = Path.of("").toAbsolutePath().normalize();
            require(clean(root), "TestKit artifact refresh requires a clean committed tree");
            RepositoryConfiguration configuration = new RepositoryConfiguration(root);
            configuration.load();
            new ModuleBuild(root, root.resolve(".worldline/build"), configuration.values(),
                    configuration.modules()).compileAll();
            Process packaging = new ProcessBuilder("java", "tools/testkit/TestKitPackage.java")
                    .directory(root.toFile()).inheritIO().start();
            require(packaging.waitFor() == 0, "exact TestKit packaging failed");
            Path output = root.resolve(".worldline/dist/testkit");
            Properties release = StrictProperties.load(root.resolve("release/testkit.properties"));
            Properties lock = new Properties();
            lock.setProperty("schema", "1");
            lock.setProperty("version", required(release, "version"));
            for (String artifact : ARTIFACTS)
                seal(lock, output, artifact, required(release, "version"));
            write(root.resolve("release/testkit-artifacts.lock"), lock);
            TestKitReleasePinCheck.validateDirectory(root, output);
            System.out.println("TestKit artifact pins refreshed");
        } catch (Exception error) {
            System.err.println("TestKit artifact pin refresh failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void seal(Properties lock, Path output, String artifact, String version)
            throws Exception {
        String prefix = "artifact." + artifact + ".";
        String file = "worldline-test-" + artifact + "-" + version + ".jar";
        Path path = output.resolve(file);
        require(Files.isRegularFile(path), "missing exact TestKit artifact: " + artifact);
        lock.setProperty(prefix + "file", file);
        lock.setProperty(prefix + "bytes", Long.toString(Files.size(path)));
        lock.setProperty(prefix + "sha256", digest(Files.readAllBytes(path)));
        int classes = 0, count = 0;
        try (JarFile jar = new JarFile(path.toFile())) {
            for (var entry : jar.stream().filter(item -> !item.isDirectory())
                    .sorted(Comparator.comparing(java.util.jar.JarEntry::getName)).toList()) {
                try (InputStream input = jar.getInputStream(entry)) {
                    lock.setProperty(prefix + "entry." + entry.getName(), digest(input.readAllBytes()));
                }
                if (entry.getName().endsWith(".class")) classes++;
                count++;
            }
        }
        require(classes > 0 && count > classes, "empty TestKit artifact: " + artifact);
        lock.setProperty(prefix + "class.count", Integer.toString(classes));
        lock.setProperty(prefix + "entry.count", Integer.toString(count));
    }

    private static void write(Path path, Properties values) throws Exception {
        StringBuilder text = new StringBuilder("# Exact deterministic Worldline TestKit artifacts\n");
        for (String key : values.stringPropertyNames().stream().sorted().toList())
            text.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, text.toString(), StandardCharsets.UTF_8);
    }

    private static String digest(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
    private static boolean clean(Path root) throws Exception {
        Process process = new ProcessBuilder("git", "status", "--porcelain",
                "--untracked-files=all").directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return process.waitFor() == 0 && output.isBlank();
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
