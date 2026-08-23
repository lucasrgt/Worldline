import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** Validates the static TestKit 0.3 release boundary without claiming runtime qualification. */
final class TestKitReleasePinCheck {
    private TestKitReleasePinCheck() { }
    static void execute(Path root) throws Exception {
        Properties lock = manifest(root), release = load(root.resolve(required(lock, "release.path")));
        require("1".equals(lock.getProperty("schema"))
                        && "0.3.0".equals(lock.getProperty("version"))
                        && lock.getProperty("version").equals(release.getProperty("version"))
                        && "release-ready".equals(release.getProperty("status"))
                        && digest(root.resolve(required(lock, "release.path"))).equals(
                                required(lock, "release.sha256")),
                "invalid TestKit release boundary");
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
        System.out.println("  TestKit 0.3 boundary: static-ready, 4 runtime qualifications pending");
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
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readString(path, StandardCharsets.UTF_8)
                    .replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8))); }
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
