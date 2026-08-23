import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** Validates token-equivalent formatting attestations and their transported proofs. */
final class FormattingPinCheck {
    private FormattingPinCheck() { }
    static void execute(Path root) throws Exception {
        Properties manifest = manifest(root);
        require("1".equals(manifest.getProperty("schema")), "invalid formatting migration schema");
        require("clang-format".equals(manifest.getProperty("formatter.name"))
                        && "22.1.0".equals(manifest.getProperty("formatter.version"))
                        && hash(manifest, "formatter.sha256"), "invalid formatter identity");
        int files = integer(manifest, "file.count");
        for (int index = 0; index < files; index++) {
            String stem = "file." + index + ".", relative = required(manifest, stem + "path");
            require(relative.matches("(?:tools/smoke|smokes)/.+[.]java"),
                    "unsafe formatting migration path: " + relative);
            String source = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
            require(digest(source).equals(required(manifest, stem + "current_sha256"))
                            && digest(FormattingPinMigration.tokens(source)).equals(
                                    required(manifest, stem + "token_sha256"))
                            && hash(manifest, stem + "prior_sha256"),
                    "formatted source drift: " + relative);
        }
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); int checked = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            checked++; String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            require(pin != null && carries(manifest, smoke.id, pin, current)
                            && hash(manifest, "smoke." + smoke.id + ".prior_fingerprint"),
                    "formatting proof drift: " + smoke.id);
        }
        require(checked == integer(manifest, "smoke.count") && checked == 525,
                "formatting proof census drift");
        System.out.println("  formatting proof transport: " + files + " files, 525 smoke inputs");
    }
    static Properties manifest(Path root) throws Exception {
        Path path = root.resolve("smokes/formatting-migration.lock");
        if (!Files.isRegularFile(path)) return new Properties();
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); }
        return values;
    }
    static boolean carries(Properties manifest, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        return hash(manifest, stem + "prior_fingerprint")
                && current.equals(manifest.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(manifest.getProperty(stem + "evidence_sha256"));
    }
    static boolean follows(Properties manifest, String id, String prior, String evidence,
            SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        return carries(manifest, id, pin, current)
                && prior.equals(manifest.getProperty(stem + "prior_fingerprint"))
                && evidence.equals(manifest.getProperty(stem + "evidence_sha256"));
    }
    static boolean transportsFile(Properties manifest, Path root, String relative, String prior)
            throws Exception {
        int files = integer(manifest, "file.count");
        for (int index = 0; index < files; index++) {
            String stem = "file." + index + ".";
            if (!relative.equals(manifest.getProperty(stem + "path"))) continue;
            String current = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
            return prior.equals(manifest.getProperty(stem + "prior_sha256"))
                    && digest(current).equals(manifest.getProperty(stem + "current_sha256"));
        }
        return false;
    }
    private static String digest(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.replace("\r\n", "\n")
                    .getBytes(StandardCharsets.UTF_8))); }
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
