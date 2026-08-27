import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Validates telemetry-only proof transport and source attestations. */
final class TelemetryPinCheck {
    private TelemetryPinCheck() { }
    static void execute(Path root) throws Exception {
        Path path = root.resolve("smokes/telemetry-migration.lock");
        if (!Files.isRegularFile(path)) return;
        Properties manifest = load(path); require("1".equals(manifest.getProperty("schema")),
                "invalid telemetry migration schema");
        for (String key : new String[] {"await_source", "process_source", "execution_source",
                "history_source", "fingerprint_source", "policy"}) {
            String relative = required(manifest, key + ".path");
            require(digest(root.resolve(relative)).equals(required(manifest, key + ".sha256"))
                            || key.equals("history_source") && SmokeScheduleBaselineCheck.transports(
                                    root, relative, required(manifest, key + ".sha256"))
                            || TrainPinCheck.transportsFile(TrainPinCheck.manifest(root), root,
                                    relative, required(manifest, key + ".sha256")),
                    "telemetry migration source drift: " + relative);
        }
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties schemas = SchemaPinCheck.manifest(root);
        Properties train = TrainPinCheck.manifest(root);
        Properties provider = ProviderDiscoveryPinCheck.manifest(root);
        int changed = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String stem = "smoke." + smoke.id + ".";
            if (manifest.getProperty(stem + "current_fingerprint") == null) continue;
            if (ProviderDiscoveryPinCheck.exemptsLegacy(provider, smoke.id)) continue;
            changed++; String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            boolean direct = current.equals(required(manifest, stem + "current_fingerprint"));
            boolean successor = pin != null && SchemaPinCheck.follows(schemas, smoke.id,
                    required(manifest, stem + "current_fingerprint"),
                    required(manifest, stem + "evidence_sha256"), pin, current);
            successor |= TrainPinCheck.carriesCurrent(train, smoke.id, pin, current);
            require(hash(manifest, stem + "prior_fingerprint")
                            && (direct || successor)
                            && hash(manifest, stem + "evidence_sha256"),
                    "telemetry migration evidence drift: " + smoke.id);
            require(pin != null && (pin.source().equals("executed")
                            || pin.source().equals("refactor-equivalent")
                            && (pin.evidence().equals(required(manifest, stem + "evidence_sha256"))
                            || SchemaPinCheck.carries(schemas, smoke.id, pin, current)
                            || TrainPinCheck.carriesCurrent(train, smoke.id, pin, current))),
                    "telemetry migration pin drift: " + smoke.id);
        }
        require(changed == integer(manifest, "count")
                        - ProviderDiscoveryPinCheck.pendingCount(provider) && changed >= 100,
                "telemetry migration census drift");
        System.out.println("  telemetry proof transport: " + changed + " smoke inputs");
    }
    static Properties manifest(Path root) throws Exception {
        Path path = root.resolve("smokes/telemetry-migration.lock");
        return Files.isRegularFile(path) ? load(path) : new Properties();
    }
    static boolean carries(Properties manifest, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        boolean direct = hash(manifest, stem + "prior_fingerprint")
                && current.equals(manifest.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(manifest.getProperty(stem + "evidence_sha256"));
        try { Path root = Path.of("").toAbsolutePath().normalize();
            return direct || SchemaPinCheck.follows(SchemaPinCheck.manifest(root), id,
                manifest.getProperty(stem + "current_fingerprint"),
                manifest.getProperty(stem + "evidence_sha256"), pin, current)
                || TrainPinCheck.carriesCurrent(TrainPinCheck.manifest(root), id, pin, current); }
        catch (Exception error) { return false; }
    }
    private static boolean hash(Properties values, String key) {
        return values.getProperty(key, "").matches("[0-9a-f]{64}");
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String digest(Path path) throws Exception { return java.util.HexFormat.of().formatHex(
            java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values;
    }
    private static String required(Properties values, String key) { String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
