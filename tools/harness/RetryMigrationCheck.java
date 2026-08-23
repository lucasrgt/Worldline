import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** Validates every exceptional retry source and its carried-forward proof. */
final class RetryMigrationCheck {
    private RetryMigrationCheck() { }

    static void execute(Path root) throws Exception {
        Properties manifest = load(root.resolve("smokes/eof-retry-migration.lock"));
        require("1".equals(manifest.getProperty("schema")) && integer(manifest, "count") == 33,
                "invalid EOF retry migration manifest");
        require(digest(root.resolve("tools/harness/SmokeRetryBoundary.java")).equals(
                manifest.getProperty("boundary_sha256")), "EOF retry boundary drift");
        require(digest(root.resolve("tools/harness/ExceptionalSmokeSupport.java")).equals(
                manifest.getProperty("support_sha256")), "exceptional smoke support drift");
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); int checked = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String stem = "retry." + smoke.id + ".";
            if (manifest.getProperty(stem + "source") == null) continue;
            checked++; Path source = root.resolve(manifest.getProperty(stem + "source")).normalize();
            require(source.startsWith(root.resolve("tools/smoke")) && Files.isRegularFile(source),
                    "missing migrated retry source: " + smoke.id);
            String fingerprint = fingerprints.compute(smoke);
            require(hash(manifest, stem + "prior_source_sha256")
                            && hash(manifest, stem + "prior_fingerprint")
                            && fingerprint.equals(manifest.getProperty(stem + "current_fingerprint"))
                            && digest(source).equals(manifest.getProperty(stem + "current_source_sha256")),
                    "EOF retry migration evidence drift: " + smoke.id);
            SmokePins.Entry pin = pins.match(smoke.id, fingerprint);
            require(pin != null && (pin.source().equals("executed")
                            || pin.source().equals("refactor-equivalent")
                            && pin.evidence().equals(manifest.getProperty(stem + "evidence_sha256"))),
                    "EOF retry migration pin drift: " + smoke.id);
        }
        require(checked == 33, "EOF retry migration census drift: " + checked);
        System.out.println("  centralized EOF retries: " + checked + " exceptional coordinators");
    }

    private static boolean hash(Properties values, String key) {
        return values.getProperty(key, "").matches("[0-9a-f]{64}");
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(values.getProperty(key, "")); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
