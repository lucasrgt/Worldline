import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;
import java.util.regex.Pattern;

/** Validates shared fixture/parser consolidation and transported smoke observations. */
final class SharedHelperPinCheck {
    private static final Pattern CLONE = Pattern.compile(
            "(?m)^  private static (?:void awaitPlayers|int local|boolean water|String sha|"
            + "BlockPosition place)\\(");
    private SharedHelperPinCheck() { }

    static void execute(Path root) throws Exception {
        Properties lock = manifest(root); require("1".equals(lock.getProperty("schema")),
                "invalid shared-helper migration schema");
        int files = integer(lock, "file.count");
        Properties gui = GuiWorkbenchPinCheck.manifest(root);
        for (int index = 0; index < files; index++) {
            String stem = "file." + index + ".", relative = required(lock, stem + "path");
            require(relative.matches("smokes/.+[.]java")
                            && (digest(root.resolve(relative)).equals(required(lock,
                                    stem + "current_sha256"))
                            || refreshes(lock, root, relative,
                                    required(lock, stem + "current_sha256"))
                            || GuiWorkbenchPinCheck.transportsFile(gui, root, relative,
                                    required(lock, stem + "current_sha256")))
                            && hash(lock, stem + "prior_sha256"),
                    "shared-helper source drift: " + relative);
        }
        for (String key : new String[] {"fixture", "aero_parser", "aero_test", "combat", "login"}) {
            String relative = required(lock, key + ".path");
            require(digest(root.resolve(relative)).equals(required(lock, key + ".current_sha256")),
                    "shared helper drift: " + relative);
        }
        int refreshed = Integer.parseInt(lock.getProperty("refresh.count", "0"));
        require(refreshed >= 1 && refreshed <= 16, "shared-helper refresh census drift");
        for (int index = 1; index <= refreshed; index++) {
            String stem = "refresh." + index + ".", relative = required(lock, stem + "path");
            require(relative.startsWith("smokes/" + required(lock, stem + "id") + "/")
                            && hash(lock, stem + "prior_sha256")
                            && digest(root.resolve(relative)).equals(
                                    required(lock, stem + "current_sha256")),
                    "refreshed shared-helper source drift: " + relative);
        }
        require(canonicalClones(root) == 0, "canonical shared fixture clone ratchet regressed");
        require(variants(root) <= integer(lock, "variant.count"),
                "shared fixture variant ratchet regressed");
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root); int checked = 0;
        Properties provider = ProviderDiscoveryPinCheck.manifest(root);
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (ProviderDiscoveryPinCheck.exemptsLegacy(provider, smoke.id)) continue;
            checked++; String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            require(pin != null && carries(lock, smoke.id, pin, current),
                    "shared-helper proof drift: " + smoke.id);
        }
        require(checked == integer(lock, "smoke.count")
                        - ProviderDiscoveryPinCheck.pendingCount(provider) && files == 354,
                "shared-helper proof census drift");
        System.out.println("  shared-helper proof transport: 354 files, " + checked + " smoke inputs");
    }

    static Properties manifest(Path root) throws Exception {
        Path path = root.resolve("smokes/shared-helper-migration.lock");
        if (!Files.isRegularFile(path)) return new Properties();
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    static boolean carries(Properties lock, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        boolean direct = hash(lock, stem + "prior_fingerprint")
                && current.equals(lock.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(lock.getProperty(stem + "evidence_sha256"));
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            Properties unicode = UnicodePinCheck.manifest(root);
            return direct || UnicodePinCheck.follows(unicode, id,
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
    static boolean transportsFile(Properties lock, Path root, String relative, String prior)
            throws Exception {
        int files = integer(lock, "file.count");
        for (int index = 0; index < files; index++) {
            String stem = "file." + index + ".";
            if (!relative.equals(lock.getProperty(stem + "path"))) continue;
            String intermediate = lock.getProperty(stem + "current_sha256");
            return prior.equals(lock.getProperty(stem + "prior_sha256"))
                    && (digest(root.resolve(relative)).equals(intermediate)
                    || refreshes(lock, root, relative, intermediate));
        }
        for (String key : new String[] {"combat", "login"})
            if (relative.equals(lock.getProperty(key + ".path")))
                return prior.equals(lock.getProperty(key + ".prior_sha256"))
                        && digest(root.resolve(relative)).equals(lock.getProperty(key + ".current_sha256"));
        int refreshed = Integer.parseInt(lock.getProperty("refresh.count", "0"));
        for (int index = 1; index <= refreshed; index++) {
            String stem = "refresh." + index + ".";
            if (relative.equals(lock.getProperty(stem + "path")))
                return prior.equals(lock.getProperty(stem + "prior_sha256"))
                        && digest(root.resolve(relative)).equals(
                                lock.getProperty(stem + "current_sha256"));
        }
        return false;
    }
    private static boolean refreshes(Properties lock, Path root, String relative, String prior)
            throws Exception {
        int count = Integer.parseInt(lock.getProperty("refresh.count", "0"));
        for (int index = 1; index <= count; index++) {
            String stem = "refresh." + index + ".";
            if (relative.equals(lock.getProperty(stem + "path")))
                return prior.equals(lock.getProperty(stem + "prior_sha256"))
                        && digest(root.resolve(relative)).equals(
                                lock.getProperty(stem + "current_sha256"));
        }
        return false;
    }
    private static long canonicalClones(Path root) throws Exception {
        try (var paths = Files.walk(root.resolve("smokes"))) {
            return paths.filter(path -> path.toString().endsWith(".java")).filter(path -> {
                try { String source = Files.readString(path);
                    return !SharedFixturePatch.rewrite(source).equals(source); }
                catch (Exception error) { throw new IllegalStateException(error); }
            }).count();
        }
    }
    private static long variants(Path root) throws Exception {
        long result = 0;
        try (var paths = Files.walk(root.resolve("smokes"))) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".java")).toList()) {
                var matcher = CLONE.matcher(Files.readString(path)); while (matcher.find()) result++;
            }
        }
        return result;
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readString(path,
                    StandardCharsets.UTF_8).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8))); }
    private static boolean hash(Properties values, String key) {
        return values.getProperty(key, "").matches("[0-9a-f]{64}");
    }
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
