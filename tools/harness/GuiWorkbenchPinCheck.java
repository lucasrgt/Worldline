import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Properties;

/** Validates the additive workbench GUI boundary and its explicit runtime-pending set. */
final class GuiWorkbenchPinCheck {
    private GuiWorkbenchPinCheck() { }
    static void execute(Path root) throws Exception {
        Properties lock = manifest(root);
        require("1".equals(lock.getProperty("schema")) && integer(lock, "source.count") == 9
                        && integer(lock, "pending.count") == 5
                        && integer(lock, "smoke.count") == 520
                        && integer(lock, "catalog.count") == 526,
                "invalid GUI workbench migration");
        require("runtime-pending".equals(lock.getProperty("release.status"))
                        && hash(lock.getProperty("release.prior_signature"))
                        && hash(lock.getProperty("release.current_signature")),
                "invalid GUI release transition");
        for (int index = 0; index < 9; index++) {
            String stem = "source." + index + ".", relative = required(lock, stem + "path");
            String baseline = required(lock, stem + "current_sha256");
            require(hash(lock.getProperty(stem + "prior_sha256"))
                            && (digest(root.resolve(relative)).equals(baseline)
                            || TrainPinCheck.transportsFile(TrainPinCheck.manifest(root), root,
                                    relative, baseline)),
                    "GUI workbench source drift: " + relative);
        }
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        Properties train = TrainPinCheck.manifest(root);
        int catalog = 0, carried = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (TrainPinCheck.isAdded(train, smoke.id)) continue;
            catalog++; String current = fingerprints.compute(smoke); SmokePins.Entry pin =
                    pins.match(smoke.id, current);
            if (smoke.id.equals("m620-stationapi-testkit-driver")) {
                require(pin == null || pin.source().equals("executed"), "M620 requires executed proof");
            } else if (isPending(lock, smoke.id)) {
                SmokePins.Entry stale = pins.entry(smoke.id); String stem = "smoke." + smoke.id + ".";
                require(pin != null && pin.source().equals("executed") || pin == null && stale != null
                                && stale.fingerprint().equals(lock.getProperty(stem + "prior_fingerprint"))
                                && stale.evidence().equals(lock.getProperty(stem + "evidence_sha256")),
                        "GUI runtime-pending proof drift: " + smoke.id);
            } else {
                carried++; require(pin != null && carries(lock, smoke.id, pin, current),
                        "GUI workbench proof drift: " + smoke.id);
            }
        }
        require(catalog == 526 && carried == 520 && integer(lock, "smoke.changed") > 0,
                "GUI workbench proof census drift");
        int pending = effectivePending(lock, train);
        System.out.println("  GUI workbench proof transport: 9 sources, 520 carried, "
                + pending + " pending");
    }
    static Properties manifest(Path root) throws Exception {
        Path path = root.resolve("smokes/gui-workbench.lock");
        if (!Files.isRegularFile(path)) return new Properties();
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values;
    }
    static boolean isPending(Properties lock, String id) {
        return Arrays.asList(lock.getProperty("pending.smokes", "").split(",")).contains(id);
    }
    static int additionalPendingCount(Properties lock) {
        return lock.getProperty("additional.pending", "").isBlank() ? 0 : 1;
    }
    private static int effectivePending(Properties lock, Properties train) {
        int count = 0;
        for (String id : lock.getProperty("pending.smokes", "").split(","))
            if (!id.isBlank() && TrainPinCheck.isPending(train, id)) count++;
        return count;
    }
    static boolean carries(Properties lock, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        boolean direct = hash(lock.getProperty(stem + "prior_fingerprint"))
                && current.equals(lock.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(lock.getProperty(stem + "evidence_sha256"));
        try { return direct || BehaviorFamilyPinCheck.follows(BehaviorFamilyPinCheck.manifest(
                Path.of("").toAbsolutePath().normalize()), id,
                lock.getProperty(stem + "current_fingerprint"),
                lock.getProperty(stem + "evidence_sha256"), pin, current); }
        catch (Exception error) { return false; }
    }
    static boolean follows(Properties lock, String id, String prior, String evidence,
            SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        return carries(lock, id, pin, current)
                && TrainPinCheck.continues(lock, id, prior, evidence);
    }
    static boolean pendingFrom(Properties lock, String id, String prior, String evidence,
            SmokePins.Entry stale) {
        String stem = "smoke." + id + ".";
        return isPending(lock, id) && stale != null
                && stale.fingerprint().equals(lock.getProperty(stem + "prior_fingerprint"))
                && stale.evidence().equals(lock.getProperty(stem + "evidence_sha256"))
                && prior.equals(stale.fingerprint()) && evidence.equals(stale.evidence());
    }
    static boolean transportsFile(Properties lock, Path root, String relative, String prior)
            throws Exception {
        for (int index = 0; index < integer(lock, "source.count"); index++) {
            String stem = "source." + index + ".";
            if (relative.equals(lock.getProperty(stem + "path")))
                return prior.equals(lock.getProperty(stem + "prior_sha256"))
                        && digest(root.resolve(relative)).equals(lock.getProperty(stem + "current_sha256"));
        }
        return false;
    }
    static boolean transitionsFile(Properties lock, String relative, String prior, String current) {
        for (int index = 0; index < integer(lock, "source.count"); index++) {
            String stem = "source." + index + ".";
            if (relative.equals(lock.getProperty(stem + "path")))
                return prior.equals(lock.getProperty(stem + "prior_sha256"))
                        && current.equals(lock.getProperty(stem + "current_sha256"));
        }
        return false;
    }
    static boolean releaseTransition(Path root, String released, String candidate) throws Exception {
        Properties lock = manifest(root);
        return "runtime-pending".equals(lock.getProperty("release.status"))
                && released.equals(lock.getProperty("release.prior_signature"))
                && candidate.equals(lock.getProperty("release.current_signature"));
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readString(path, StandardCharsets.UTF_8)
                    .replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8))); }
    private static boolean hash(String value) { return value != null && value.matches("[0-9a-f]{64}"); }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) { String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key); return value; }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
