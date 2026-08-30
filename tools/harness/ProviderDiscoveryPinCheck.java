import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** Validates provider-discovery sources and proof transport for pre-existing smokes. */
final class ProviderDiscoveryPinCheck {
    private ProviderDiscoveryPinCheck() { }

    static void execute(Path root) throws Exception {
        Properties lock = manifest(root);
        require("1".equals(lock.getProperty("schema"))
                        && integer(lock, "modified.count") == 9
                        && integer(lock, "added.count") == 12
                        && integer(lock, "smoke.count") >= 521
                        && integer(lock, "pending.count") == 4
                        && integer(lock, "catalog.count") == integer(lock, "smoke.count")
                                + integer(lock, "pending.count") + 1,
                "invalid provider-discovery migration");
        sources(root, lock, "modified", 9, true); sources(root, lock, "added", 12, false);
        refreshSources(root, lock);
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        Properties gui = GuiWorkbenchPinCheck.manifest(root);
        int carried = 0, discovered = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (TrainPinCheck.isAdded(TrainPinCheck.manifest(root), smoke.id)
                    && !isNewSmoke(lock, smoke.id)) continue;
            discovered++; String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            if (isNewSmoke(lock, smoke.id)) {
                require(pin == null || pin.source().equals("executed")
                                || TrainPinCheck.isExecuted(TrainPinCheck.manifest(root), smoke.id),
                        "new provider smoke requires executed evidence");
                continue;
            }
            if (isPending(lock, smoke.id)) {
                SmokePins.Entry stale = pins.entry(smoke.id); String stem = "smoke." + smoke.id + ".";
                require(pin != null && (pin.source().equals("executed")
                                || TrainPinCheck.isExecuted(TrainPinCheck.manifest(root), smoke.id))
                                || pin == null && stale != null
                                && stale.fingerprint().equals(lock.getProperty(stem + "prior_fingerprint"))
                                && stale.evidence().equals(lock.getProperty(stem + "evidence_sha256")),
                        "runtime-pending provider proof drift: " + smoke.id);
                continue;
            }
            if (GuiWorkbenchPinCheck.isPending(gui, smoke.id)) {
                carried++; SmokePins.Entry stale = pins.entry(smoke.id); String stem = "smoke." + smoke.id + ".";
                boolean executed = TrainPinCheck.isExecuted(TrainPinCheck.manifest(root), smoke.id)
                        && pin != null;
                require(executed || GuiWorkbenchPinCheck.pendingFrom(gui, smoke.id,
                                lock.getProperty(stem + "current_fingerprint"),
                                lock.getProperty(stem + "evidence_sha256"), stale),
                        "successor GUI pending proof drift: " + smoke.id);
                continue;
            }
            carried++; require(pin != null && carries(lock, smoke.id, pin, current),
                    "provider-discovery proof drift: " + smoke.id);
        }
        require(discovered == integer(lock, "catalog.count")
                        && carried == integer(lock, "smoke.count")
                        && integer(lock, "smoke.changed") > 0,
                "provider-discovery proof census drift");
        System.out.println("  provider-discovery proof transport: 21 sources, " + carried
                + " carried, "
                + effectivePendingCount(lock, root) + " pending");
    }

    static Properties manifest(Path root) throws Exception {
        Path path = root.resolve("smokes/provider-discovery.lock");
        if (!Files.isRegularFile(path)) return new Properties();
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); }
        return values;
    }
    static boolean isNewSmoke(Properties lock, String id) {
        return id.equals(lock.getProperty("new.smoke"));
    }
    static boolean isPending(Properties lock, String id) {
        return java.util.Arrays.asList(lock.getProperty("pending.smokes", "").split(",")).contains(id);
    }
    static boolean exemptsLegacy(Properties lock, String id) {
        if (isNewSmoke(lock, id) || isPending(lock, id)) return true;
        try { Path root = Path.of("").toAbsolutePath().normalize();
            return GuiWorkbenchPinCheck.isPending(GuiWorkbenchPinCheck.manifest(root), id)
                    || TrainPinCheck.isAdded(TrainPinCheck.manifest(root), id); }
        catch (Exception error) { return false; }
    }
    static int pendingCount(Properties lock) {
        try { return integer(lock, "pending.count") + GuiWorkbenchPinCheck.additionalPendingCount(
                GuiWorkbenchPinCheck.manifest(Path.of("").toAbsolutePath().normalize())); }
        catch (Exception error) { return integer(lock, "pending.count"); }
    }
    private static int effectivePendingCount(Properties lock, Path root) throws Exception {
        Properties train = TrainPinCheck.manifest(root); int count = 0;
        for (String id : lock.getProperty("pending.smokes", "").split(","))
            if (!id.isBlank() && TrainPinCheck.isPending(train, id)) count++;
        String additional = GuiWorkbenchPinCheck.manifest(root)
                .getProperty("additional.pending", "");
        return count + (!additional.isBlank() && TrainPinCheck.isPending(train, additional) ? 1 : 0);
    }
    static boolean carries(Properties lock, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        boolean introduced = "true".equals(lock.getProperty(stem + "introduced"))
                && "executed".equals(pin.source());
        boolean direct = (hash(lock.getProperty(stem + "prior_fingerprint")) || introduced)
                && current.equals(lock.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(lock.getProperty(stem + "evidence_sha256"));
        try { Path root = Path.of("").toAbsolutePath().normalize();
            return direct || GuiWorkbenchPinCheck.follows(GuiWorkbenchPinCheck.manifest(root), id,
                lock.getProperty(stem + "current_fingerprint"),
                lock.getProperty(stem + "evidence_sha256"), pin, current)
                || TrainPinCheck.carriesCurrent(TrainPinCheck.manifest(root), id, pin, current)
                || SchemaPinCheck.carries(SchemaPinCheck.manifest(root), id, pin, current); }
        catch (Exception error) { return false; }
    }
    static boolean follows(Properties lock, String id, String prior, String evidence,
            SmokePins.Entry pin, String current) {
        if (prior == null || evidence == null) return false;
        String stem = "smoke." + id + ".";
        return carries(lock, id, pin, current)
                && TrainPinCheck.continues(lock, id, prior, evidence);
    }

    private static void sources(Path root, Properties lock, String group, int count,
            boolean prior) throws Exception {
        for (int index = 0; index < count; index++) {
            String stem = group + "." + index + ".", relative = required(lock, stem + "path");
            String baseline = required(lock, stem + "current_sha256");
            require((digest(root.resolve(relative)).equals(baseline)
                            || TrainPinCheck.transportsFile(TrainPinCheck.manifest(root), root,
                                    relative, baseline)
                            || SchemaPinCheck.transportsFile(root, relative, baseline))
                            && (!prior || hash(lock.getProperty(stem + "prior_sha256"))),
                    "provider-discovery source drift: " + relative);
        }
    }
    private static void refreshSources(Path root, Properties lock) throws Exception {
        Properties train = TrainPinCheck.manifest(root);
        int count = integer(lock, "refresh.source.count");
        require(count >= 1 && count <= 16, "provider source refresh census drift");
        for (int index = 1; index <= count; index++) {
            String stem = "refresh.source." + index + ".";
            String relative = required(lock, stem + "path");
            String prior = required(lock, stem + "prior_sha256");
            String current = required(lock, stem + "current_sha256");
            boolean fingerprint = relative.equals("tools/harness/SmokeInputFingerprint.java")
                    && Files.readString(root.resolve(relative)).contains("src/(?:main|testkit)/java")
                    && Files.readString(root.resolve("tools/harness/DataDrivenCyclePlan.java"))
                            .contains("src/(?:main|testkit)/java");
            boolean successor = SchemaPinCheck.transportsFile(root, relative, current);
            require(hash(prior) && (digest(root.resolve(relative)).equals(current)
                            && (fingerprint
                            || TrainPinCheck.transportsFile(train, root, relative, prior))
                            || successor),
                    "invalid provider source refresh: " + relative);
        }
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readString(path,
                    StandardCharsets.UTF_8).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8))); }
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
