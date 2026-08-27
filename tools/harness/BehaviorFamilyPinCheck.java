import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Properties;

/** Validates the placement taxonomy and its content-addressed proof transport. */
final class BehaviorFamilyPinCheck {
    private BehaviorFamilyPinCheck() { }

    static void execute(Path root) throws Exception {
        Properties lock = manifest(root); Map<String, String> assignments = BehaviorFamilyAssignments.values();
        require("1".equals(lock.getProperty("schema")) && integer(lock, "assignment.count") == 109
                        && integer(lock, "source.count") == 4 && integer(lock, "smoke.count") >= 520
                        && integer(lock, "pending.count") == 5
                        && integer(lock, "catalog.count") == integer(lock, "smoke.count")
                                + integer(lock, "pending.count") + 1,
                "invalid behavior-family migration");
        for (int index = 0; index < 4; index++) {
            String stem = "source." + index + ".", relative = required(lock, stem + "path");
            String prior = required(lock, stem + "prior_sha256");
            require((hash(prior) || "added".equals(prior))
                            && (digest(root.resolve(relative)).equals(required(lock, stem + "current_sha256"))
                            || TrainPinCheck.transportsFile(TrainPinCheck.manifest(root), root,
                                    relative, required(lock, stem + "current_sha256"))),
                    "behavior-family source drift: " + relative);
        }
        refreshSources(root, lock);
        int index = 0;
        for (Map.Entry<String, String> entry : assignments.entrySet()) {
            String stem = "assignment." + index++ + ".";
            require(entry.getKey().equals(required(lock, stem + "id"))
                            && entry.getValue().equals(required(lock, stem + "token"))
                            && hash(required(lock, stem + "prior_sha256"))
                            && digest(root.resolve("smokes").resolve(entry.getKey()).resolve("smoke.properties"))
                                    .equals(required(lock, stem + "current_sha256"))
                            && behavior(root, entry.getKey()).equals(entry.getValue()),
                    "behavior-family assignment drift: " + entry.getKey());
        }
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root); int catalog = 0, carried = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (TrainPinCheck.isAdded(TrainPinCheck.manifest(root), smoke.id)) continue;
            catalog++; String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            if (smoke.id.equals("m620-stationapi-testkit-driver"))
                require(pin == null || pin.source().equals("executed"), "M620 requires executed proof");
            else if (pending(lock, smoke.id)) {
                SmokePins.Entry stale = pins.entry(smoke.id); String stem = "smoke." + smoke.id + ".";
                require(pin != null && pin.source().equals("executed") || pin == null && stale != null
                                && stale.fingerprint().equals(required(lock, stem + "prior_fingerprint"))
                                && stale.evidence().equals(required(lock, stem + "evidence_sha256")),
                        "behavior-family pending proof drift: " + smoke.id);
            } else {
                carried++; require(pin != null && carries(lock, smoke.id, pin, current),
                        "behavior-family proof drift: " + smoke.id);
            }
        }
        require(catalog == integer(lock, "catalog.count")
                        && carried == integer(lock, "smoke.count")
                        && integer(lock, "smoke.changed") > 0,
                "behavior-family proof census drift");
        System.out.println("  behavior-family proof transport: 109 assignments, " + carried + " carried");
    }

    static boolean follows(Properties lock, String id, String prior, String evidence,
            SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        return carries(lock, id, pin, current)
                && TrainPinCheck.continues(lock, id, prior, evidence);
    }
    static boolean transportsDescriptor(Properties lock, Path root, String id, String prior)
            throws Exception {
        int index = 0;
        for (Map.Entry<String, String> entry : BehaviorFamilyAssignments.values().entrySet()) {
            String stem = "assignment." + index++ + ".";
            if (entry.getKey().equals(id)) return prior.equals(lock.getProperty(stem + "prior_sha256"))
                    && digest(root.resolve("smokes").resolve(id).resolve("smoke.properties"))
                            .equals(lock.getProperty(stem + "current_sha256"));
        }
        return false;
    }
    private static boolean carries(Properties lock, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        boolean introduced = "true".equals(lock.getProperty(stem + "introduced"))
                && "executed".equals(pin.source());
        boolean direct = (hash(lock.getProperty(stem + "prior_fingerprint")) || introduced)
                && current.equals(lock.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(lock.getProperty(stem + "evidence_sha256"));
        try { Path root = Path.of("").toAbsolutePath().normalize();
            Properties train = TrainPinCheck.manifest(root);
            return direct || TrainPinCheck.follows(train, id,
                lock.getProperty(stem + "current_fingerprint"),
                lock.getProperty(stem + "evidence_sha256"), pin, current)
                || TrainPinCheck.carriesCurrent(train, id, pin, current); }
        catch (Exception error) { return false; }
    }
    static Properties manifest(Path root) throws Exception {
        Properties values = new Properties(); Path path = root.resolve("smokes/behavior-family-rebalance.lock");
        if (Files.isRegularFile(path)) try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static String behavior(Path root, String id) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(root.resolve("smokes").resolve(id)
                .resolve("smoke.properties"), StandardCharsets.UTF_8)) { values.load(reader); }
        return values.getProperty("behavior", "").trim();
    }
    private static boolean pending(Properties lock, String id) {
        return Arrays.asList(lock.getProperty("pending.smokes", "").split(",")).contains(id);
    }
    private static void refreshSources(Path root, Properties lock) throws Exception {
        int count = integer(lock, "refresh.source.count");
        require(count >= 1 && count <= 4, "behavior-family source refresh census drift");
        for (int index = 1; index <= count; index++) {
            String stem = "refresh.source." + index + ".";
            String relative = required(lock, stem + "path");
            require(relative.equals("modules/api/src/main/java/worldline/api/WorldlineBehavior.java")
                            && hash(required(lock, stem + "prior_sha256"))
                            && digest(root.resolve(relative)).equals(
                                    required(lock, stem + "current_sha256"))
                            && Files.readString(root.resolve(relative))
                                    .contains("BLOCK_LIFECYCLE_CONFORMANCE"),
                    "invalid behavior-family source refresh");
        }
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
