import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;
import java.util.jar.JarFile;

/** Validates the static TestKit 0.3 release boundary without claiming runtime qualification. */
final class TestKitReleasePinCheck {
    private TestKitReleasePinCheck() { }
    static void execute(Path root) throws Exception {
        Properties lock = manifest(root), release = load(root.resolve(required(lock, "release.path")));
        require("1".equals(lock.getProperty("schema"))
                        && lock.getProperty("version", "").matches("0[.]3[.][0-9]+")
                        && lock.getProperty("version").equals(release.getProperty("version"))
                        && "release-ready".equals(release.getProperty("status"))
                        && digest(root.resolve(required(lock, "release.path"))).equals(
                                required(lock, "release.sha256")),
                "invalid TestKit release boundary");
        validateArtifactLock(root, release);
        for (String key : new String[] {"source", "descriptor", "map"}) {
            String relative = required(lock, key + ".path");
            Path path = root.resolve(relative);
            String current = required(lock, key + ".current_sha256");
            boolean direct = digest(path).equals(current);
            boolean successor = TrainPinCheck.transportsFile(
                    TrainPinCheck.manifest(root), root, relative, current);
            require(hash(lock.getProperty(key + ".prior_sha256"))
                            && (direct || successor),
                    "TestKit release source drift: " + root.relativize(path));
        }
        Properties provider = ProviderDiscoveryPinCheck.manifest(root);
        require(integer(lock, "pending.count") == 4
                        && lock.getProperty("pending.smokes").equals(
                                provider.getProperty("pending.smokes")),
                "TestKit runtime-pending census drift");
        int pending = 0; Properties train = TrainPinCheck.manifest(root);
        for (String id : lock.getProperty("pending.smokes", "").split(","))
            if (!id.isBlank() && TrainPinCheck.isPending(train, id)) pending++;
        System.out.println("  TestKit 0.3 boundary: release-ready, " + pending
                + " runtime qualifications pending");
    }
    static Properties manifest(Path root) throws Exception {
        return load(root.resolve("smokes/testkit-release-0.3.lock"));
    }
    static boolean transportsFile(Properties lock, Path root, String relative, String prior)
            throws Exception {
        return relative.equals(lock.getProperty("source.path"))
                && prior.equals(lock.getProperty("source.prior_sha256"))
                && digest(root.resolve(relative)).equals(lock.getProperty("source.current_sha256"));
    }
    static boolean transitionsFile(Properties lock, String relative, String prior, String current) {
        for (String key : new String[] {"source", "descriptor", "map"}) {
            if (relative.equals(lock.getProperty(key + ".path")))
                return prior.equals(lock.getProperty(key + ".prior_sha256"))
                        && current.equals(lock.getProperty(key + ".current_sha256"));
        }
        return false;
    }
    private static void validateArtifactLock(Path root, Properties release) throws Exception {
        Properties artifacts = load(root.resolve("release/testkit-artifacts.lock"));
        require("1".equals(artifacts.getProperty("schema"))
                        && release.getProperty("version").equals(artifacts.getProperty("version")),
                "invalid TestKit artifact lock");
        int entries = 0;
        for (String artifact : new String[] {"api", "runner"}) {
            String prefix = "artifact." + artifact + ".";
            int count = integer(artifacts, prefix + "entry.count"); entries += count;
            require(integer(artifacts, prefix + "class.count") > 0
                            && Long.parseLong(required(artifacts, prefix + "bytes")) > 0
                            && hash(required(artifacts, prefix + "sha256")),
                    "invalid TestKit artifact metadata: " + artifact);
            long locked = artifacts.stringPropertyNames().stream()
                    .filter(key -> key.startsWith(prefix + "entry.")).filter(key -> {
                        String value = artifacts.getProperty(key); return hash(value); }).count();
            require(locked == count, "incomplete TestKit per-entry lock: " + artifact);
        }
        require(artifacts.size() == entries + 12, "unexpected TestKit artifact lock fields");
    }
    static void validateDirectory(Path root, Path output) throws Exception {
        Properties lock = load(root.resolve("release/testkit-artifacts.lock"));
        for (String artifact : new String[] {"api", "runner"}) {
            String prefix = "artifact." + artifact + ".";
            Path jar = output.resolve(required(lock, prefix + "file"));
            require(Files.isRegularFile(jar) && Files.size(jar) == Long.parseLong(
                    required(lock, prefix + "bytes")), "TestKit artifact size drift: " + artifact);
            require(binaryDigest(jar).equals(required(lock, prefix + "sha256")),
                    "TestKit artifact hash drift: " + artifact);
            try (JarFile archive = new JarFile(jar.toFile())) {
                var entries = archive.stream().filter(entry -> !entry.isDirectory()).toList();
                require(entries.size() == integer(lock, prefix + "entry.count"),
                        "TestKit artifact entry census drift: " + artifact);
                for (var entry : entries) try (var input = archive.getInputStream(entry)) {
                    require(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                            .digest(input.readAllBytes())).equals(required(lock,
                                    prefix + "entry." + entry.getName())),
                            "TestKit artifact entry drift: " + entry.getName());
                }
            }
        }
    }
    private static Properties load(Path path) throws Exception { return StrictProperties.load(path); }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readString(path, StandardCharsets.UTF_8)
                    .replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8))); }
    private static String binaryDigest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    private static boolean hash(String value) { return value != null && value.matches("[0-9a-f]{64}"); }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(),
                "missing " + key); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
