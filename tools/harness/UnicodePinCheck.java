import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Validates portable Unicode-normalized fingerprint proof transport. */
final class UnicodePinCheck {
    private UnicodePinCheck() { }
    static void execute(Path root) throws Exception {
        Properties lock = manifest(root);
        require("1".equals(lock.getProperty("schema"))
                        && "utf8-lf-nfc".equals(lock.getProperty("normalization")),
                "invalid Unicode normalization migration");
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root); int checked = 0;
        Properties provider = ProviderDiscoveryPinCheck.manifest(root);
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (ProviderDiscoveryPinCheck.exemptsLegacy(provider, smoke.id)) continue;
            checked++; String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            require(pin != null && carries(lock, smoke.id, pin, current),
                    "Unicode-normalized proof drift: " + smoke.id);
        }
        require(checked == integer(lock, "smoke.count")
                        - ProviderDiscoveryPinCheck.pendingCount(provider)
                        && integer(lock, "smoke.changed") > 0,
                "Unicode normalization proof census drift");
        System.out.println("  Unicode-normalized smoke proof transport: " + checked + " inputs");
    }
    static Properties manifest(Path root) throws Exception {
        Path path = root.resolve("smokes/unicode-normalization.lock");
        if (!Files.isRegularFile(path)) return new Properties();
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    static boolean carries(Properties lock, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        boolean direct = hash(lock.getProperty(stem + "prior_fingerprint"))
                && current.equals(lock.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(lock.getProperty(stem + "evidence_sha256"));
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            Properties split = AdapterSplitPinCheck.manifest(root);
            return direct || AdapterSplitPinCheck.follows(split, id,
                    lock.getProperty(stem + "current_fingerprint"),
                    lock.getProperty(stem + "evidence_sha256"), pin, current)
                    || TrainPinCheck.carriesCurrent(
                            TrainPinCheck.manifest(root), id, pin, current);
        } catch (Exception error) { return false; }
    }
    static boolean follows(Properties lock, String id, String prior, String evidence,
            SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        return carries(lock, id, pin, current)
                && TrainPinCheck.continues(lock, id, prior, evidence);
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(values.getProperty(key, "")); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static boolean hash(String value) { return value != null && value.matches("[0-9a-f]{64}"); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
