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
        Properties dataDriven = load(root.resolve("smokes/data-driven-migration.lock"));
        Properties composite = load(root.resolve("smokes/composite-cycle-migration.lock"));
        require("1".equals(manifest.getProperty("schema")) && integer(manifest, "count") == 33,
                "invalid EOF retry migration manifest");
        require(digest(root.resolve("tools/harness/SmokeRetryBoundary.java")).equals(
                manifest.getProperty("boundary_sha256")), "EOF retry boundary drift");
        require(digest(root.resolve("tools/harness/ExceptionalSmokeSupport.java")).equals(
                manifest.getProperty("support_sha256")), "exceptional smoke support drift");
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties telemetry = TelemetryPinCheck.manifest(root);
        Properties schemas = SchemaPinCheck.manifest(root);
        int checked = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String stem = "retry." + smoke.id + ".";
            if (manifest.getProperty(stem + "source") == null) continue;
            checked++; Path source = root.resolve(manifest.getProperty(stem + "source")).normalize();
            String fingerprint = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, fingerprint);
            require(source.startsWith(root.resolve("tools/smoke")), "unsafe migrated retry source");
            if (Files.isRegularFile(source)) require(digest(source).equals(
                            manifest.getProperty(stem + "current_source_sha256"))
                            && (fingerprint.equals(manifest.getProperty(stem + "current_fingerprint"))
                            || pin != null && TelemetryPinCheck.carries(
                                    telemetry, smoke.id, pin, fingerprint)
                            || pin != null && SchemaPinCheck.carries(
                                    schemas, smoke.id, pin, fingerprint)),
                    "EOF retry migration evidence drift: " + smoke.id);
            else require(successor(dataDriven, composite, smoke.id, manifest, stem),
                    "missing migrated retry successor: " + smoke.id);
            require(hash(manifest, stem + "prior_source_sha256")
                            && hash(manifest, stem + "prior_fingerprint"),
                    "EOF retry migration hashes drift: " + smoke.id);
            require(pin != null && (pin.source().equals("executed")
                            || pin.source().equals("refactor-equivalent")
                            && (pin.evidence().equals(manifest.getProperty(stem + "evidence_sha256"))
                            || TelemetryPinCheck.carries(telemetry, smoke.id, pin, fingerprint)
                            || SchemaPinCheck.carries(schemas, smoke.id, pin, fingerprint))),
                    "EOF retry migration pin drift: " + smoke.id);
        }
        require(checked == 33, "EOF retry migration census drift: " + checked);
        System.out.println("  centralized EOF retries: " + checked + " exceptional coordinators");
    }

    private static boolean successor(Properties dataDriven, Properties composite, String id,
            Properties retries, String retryStem) {
        for (Properties migration : new Properties[] {dataDriven, composite}) {
            String stem = "cycle." + id + ".";
            if (migration.getProperty(stem + "source") == null) continue;
            return retries.getProperty(retryStem + "source").equals(migration.getProperty(stem + "source"))
                    && retries.getProperty(retryStem + "current_source_sha256").equals(
                    migration.getProperty(stem + "source_sha256"))
                    && retries.getProperty(retryStem + "current_fingerprint").equals(
                    migration.getProperty(stem + "prior_fingerprint"))
                    && retries.getProperty(retryStem + "evidence_sha256").equals(
                    migration.getProperty(stem + "evidence_sha256"));
        }
        return false;
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
                .digest(Files.readString(path, StandardCharsets.UTF_8).replace("\r\n", "\n")
                        .getBytes(StandardCharsets.UTF_8)));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
