import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Validates schema migration artifacts and content-addressed proof transport. */
final class SchemaPinCheck {
    private SchemaPinCheck() { }
    static void execute(Path root) throws Exception {
        Properties manifest = manifest(root); require("1".equals(manifest.getProperty("schema")),
                "invalid repository schema migration");
        require(digest(root.resolve("tools/harness/SmokeInputFingerprint.java")).equals(
                        required(manifest, "fingerprint_source_sha256")),
                "repository schema fingerprint source drift");
        require(integer(manifest, "smoke.count") > 0
                        && integer(manifest, "map.count") == integer(manifest, "smoke.count") + 1
                        && integer(manifest, "narrative.count") >= 36,
                "repository schema migration census drift");
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties formatting = FormattingPinCheck.manifest(root);
        Properties train = TrainPinCheck.manifest(root);
        Properties provider = ProviderDiscoveryPinCheck.manifest(root);
        int checked = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (ProviderDiscoveryPinCheck.exemptsLegacy(provider, smoke.id)) continue;
            checked++; String stem = "smoke." + smoke.id + "."; String current = fingerprints.compute(smoke);
            Path directory = root.resolve("smokes").resolve(smoke.id);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            boolean direct = current.equals(required(manifest, stem + "current_fingerprint"));
            boolean successor = pin != null && FormattingPinCheck.follows(formatting, smoke.id,
                    required(manifest, stem + "current_fingerprint"),
                    required(manifest, stem + "evidence_sha256"), pin, current);
            successor |= TrainPinCheck.carriesCurrent(train, smoke.id, pin, current);
            successor |= LifecycleClaimTestKitPinCheck.carries(root, smoke.id, pin, current);
            boolean descriptor = digest(directory.resolve("smoke.properties")).equals(
                    required(manifest, stem + "descriptor_sha256"));
            if (!descriptor) descriptor = BehaviorFamilyPinCheck.transportsDescriptor(
                    BehaviorFamilyPinCheck.manifest(root), root, smoke.id,
                    required(manifest, stem + "descriptor_sha256"));
            if (!descriptor) descriptor = TrainPinCheck.transportsFile(train, root,
                    "smokes/" + smoke.id + "/smoke.properties",
                    required(manifest, stem + "descriptor_sha256"));
            if (!descriptor) descriptor = LifecycleClaimTestKitPinCheck.transportsFile(root,
                    "smokes/" + smoke.id + "/smoke.properties",
                    required(manifest, stem + "descriptor_sha256"));
            require((hash(manifest, stem + "prior_fingerprint")
                            || "true".equals(manifest.getProperty(stem + "introduced")))
                            && (direct || successor)
                            && hash(manifest, stem + "evidence_sha256")
                            && descriptor
                            && digest(directory.resolve("MAP.md")).equals(
                                    required(manifest, stem + "map_sha256")),
                    "repository schema migration drift: " + smoke.id);
            require(pin != null && (pin.source().equals("executed")
                            || pin.source().equals("refactor-equivalent"))
                            && (pin.evidence().equals(required(manifest, stem + "evidence_sha256"))
                            || FormattingPinCheck.carries(formatting, smoke.id, pin, current)
                            || TrainPinCheck.carriesCurrent(train, smoke.id, pin, current)
                            || LifecycleClaimTestKitPinCheck.carries(
                                    root, smoke.id, pin, current)),
                    "repository schema pin drift: " + smoke.id);
        }
        require(checked == integer(manifest, "smoke.count")
                        - ProviderDiscoveryPinCheck.pendingCount(provider),
                "repository schema pin census drift");
        System.out.println("  repository schema proof transport: " + checked + " smoke inputs");
    }
    static Properties manifest(Path root) throws Exception {
        Path path = root.resolve("smokes/schema-migration.lock");
        return Files.isRegularFile(path) ? load(path) : new Properties();
    }
    static boolean carries(Properties manifest, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        boolean direct = (hash(manifest, stem + "prior_fingerprint")
                || "true".equals(manifest.getProperty(stem + "introduced")))
                && current.equals(manifest.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(manifest.getProperty(stem + "evidence_sha256"));
        try { Path root = Path.of("").toAbsolutePath().normalize();
            return direct || TrainPinCheck.carriesCurrent(
                    TrainPinCheck.manifest(root), id, pin, current)
                    || LifecycleClaimTestKitPinCheck.carries(root, id, pin, current); }
        catch (Exception error) { return direct; }
    }
    static boolean introduced(Properties manifest, String id) {
        return "true".equals(manifest.getProperty("smoke." + id + ".introduced"));
    }
    static boolean transportsFile(Path root, String relative, String prior) {
        return LifecycleClaimTestKitPinCheck.transportsFile(root, relative, prior);
    }
    static boolean trainSourceSuccessor(Path root, String relative,
            String prior, String current) throws Exception {
        return TrainGeneratedDocumentationMigration.carriesSource(relative, prior, current);
    }
    static int introductionsAfter(Properties successor, Properties predecessor) {
        return (int) successor.stringPropertyNames().stream()
                .filter(key -> key.startsWith("smoke.") && key.endsWith(".introduced"))
                .filter(key -> "true".equals(successor.getProperty(key)))
                .map(key -> key.substring(6, key.length() - 11))
                .filter(id -> predecessor.getProperty(
                        "smoke." + id + ".current_fingerprint") == null)
                .count();
    }
    static void selfTest() {
        Properties prior = new Properties(), successor = new Properties();
        prior.setProperty("smoke.old.current_fingerprint", "old");
        successor.setProperty("smoke.old.introduced", "true");
        successor.setProperty("smoke.new.introduced", "true");
        successor.setProperty("smoke.carried.introduced", "false");
        require(introductionsAfter(successor, prior) == 1,
                "schema successor introduction census drift");
    }
    static boolean follows(Properties manifest, String id, String prior, String evidence,
            SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        boolean direct = carries(manifest, id, pin, current);
        boolean successor;
        try { successor = FormattingPinCheck.follows(FormattingPinCheck.manifest(
                Path.of("").toAbsolutePath().normalize()), id,
                manifest.getProperty(stem + "current_fingerprint"),
                manifest.getProperty(stem + "evidence_sha256"), pin, current); }
        catch (Exception error) { return false; }
        return (direct || successor) && TrainPinCheck.continues(manifest, id, prior, evidence);
    }
    private static boolean hash(Properties values, String key) {
        return values.getProperty(key, "").matches("[0-9a-f]{64}");
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String digest(Path path) throws Exception { return java.util.HexFormat.of().formatHex(
            java.security.MessageDigest.getInstance("SHA-256").digest(Files.readString(path,
                    StandardCharsets.UTF_8).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8)));
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
