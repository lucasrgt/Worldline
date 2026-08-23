import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Properties;

/** Validates the portable proof chain for the reconciled milestone train. */
final class TrainPinCheck {
    private TrainPinCheck() { }

    static void execute(Path root) throws Exception {
        Properties lock = manifest(root);
        require("1".equals(lock.getProperty("schema"))
                        && integer(lock, "catalog.count") == SmokeDiscovery.discover(root).size()
                        && integer(lock, "imported.count") > 0
                        && integer(lock, "carried.count") > 0,
                "invalid train reconciliation lock");
        for (int index = 0; index < integer(lock, "source.count"); index++) {
            String stem = "source." + index + ".", relative = required(lock, stem + "path");
            String current = required(lock, stem + "current_sha256"); Path path = root.resolve(relative);
            require((current.equals("removed") && !Files.exists(path))
                            || Files.isRegularFile(path) && current.equals(digest(path)),
                    "train source drift: " + relative);
        }
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        int currentCount = 0, pendingCount = 0, imported = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String stem = "smoke." + smoke.id + ".", current = fingerprints.compute(smoke);
            require(current.equals(required(lock, stem + "current_fingerprint")),
                    "train fingerprint drift: " + smoke.id);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            if (isPending(lock, smoke.id)) {
                pendingCount++; SmokePins.Entry stale = pins.entry(smoke.id);
                String prior = required(lock, stem + "prior_fingerprint");
                String evidence = required(lock, stem + "evidence_sha256");
                require(pin != null && pin.source().equals("executed")
                                || pin == null && (prior.equals("absent") && stale == null
                                || stale != null && stale.fingerprint().equals(prior)
                                && stale.evidence().equals(evidence)),
                        "train pending proof drift: " + smoke.id);
                continue;
            }
            currentCount++; require(pin != null
                            && pin.evidence().equals(required(lock, stem + "evidence_sha256")),
                    "train proof drift: " + smoke.id);
            if ("milestone".equals(required(lock, stem + "kind"))) {
                imported++; receipt(lock, stem, smoke.id);
            }
        }
        require(currentCount + pendingCount == integer(lock, "catalog.count")
                        && imported == integer(lock, "imported.count")
                        && pendingCount == integer(lock, "pending.count"),
                "train proof census drift");
        System.out.println("  train proof transport: " + currentCount + " current, "
                + imported + " milestone receipts, " + pendingCount + " pending");
    }

    static boolean isAdded(Properties lock, String id) {
        return "milestone".equals(lock.getProperty("smoke." + id + ".kind"));
    }
    static boolean isPending(Properties lock, String id) {
        return Arrays.asList(lock.getProperty("pending.smokes", "").split(",")).contains(id);
    }
    static boolean follows(Properties lock, String id, String prior, String evidence,
            SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        return !isPending(lock, id) && prior != null && evidence != null && pin != null
                && prior.equals(lock.getProperty(stem + "prior_fingerprint"))
                && current.equals(lock.getProperty(stem + "current_fingerprint"))
                && evidence.equals(lock.getProperty(stem + "evidence_sha256"))
                && pin.evidence().equals(evidence);
    }
    static boolean transportsFile(Properties lock, Path root, String relative, String prior)
            throws Exception {
        for (int index = 0; index < integer(lock, "source.count"); index++) {
            String stem = "source." + index + ".";
            if (relative.equals(lock.getProperty(stem + "path"))) {
                String predecessor = lock.getProperty(stem + "prior_sha256");
                boolean connected = prior.equals(predecessor)
                        || GuiWorkbenchPinCheck.transitionsFile(
                                GuiWorkbenchPinCheck.manifest(root), relative, prior, predecessor);
                return connected && digest(root.resolve(relative)).equals(
                        lock.getProperty(stem + "current_sha256"));
            }
        }
        return false;
    }
    static Properties manifest(Path root) throws Exception {
        Properties values = new Properties(); Path path = root.resolve("smokes/train-reconciliation.lock");
        if (Files.isRegularFile(path)) try (Reader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) { values.load(reader); }
        return values;
    }

    private static void receipt(Properties lock, String stem, String id) {
        for (String key : new String[] {"receipt.head", "receipt.tree", "receipt.base"})
            require(commitHash(lock.getProperty(stem + key)),
                    "invalid milestone receipt " + key + ": " + id);
        require(hash(lock.getProperty(stem + "receipt.signature")),
                "invalid milestone receipt signature: " + id);
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    private static boolean hash(String value) { return value != null && value.matches("[0-9a-f]{64}"); }
    private static boolean commitHash(String value) {
        return value != null && value.matches("[0-9a-f]{40}|[0-9a-f]{64}");
    }
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
