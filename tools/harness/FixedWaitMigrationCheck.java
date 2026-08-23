import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;
import java.util.regex.Pattern;

/** Validates the fixed-wait classification and its content-addressed carried proofs. */
final class FixedWaitMigrationCheck {
    private static final Pattern DEBT = Pattern.compile(
            "sustainTicks\\([^\\)]*\\)[\\s\\S]{0,160}?require\\(");
    private static final Pattern AWAIT = Pattern.compile("WorldlineSmokeAwait\\s*[.]");

    private FixedWaitMigrationCheck() { }

    static void execute(Path root) throws Exception {
        Properties manifest = load(root.resolve("smokes/fixed-wait-migration.lock"));
        Properties dataDriven = load(root.resolve("smokes/data-driven-migration.lock"));
        Properties composite = load(root.resolve("smokes/composite-cycle-migration.lock"));
        Properties telemetry = TelemetryPinCheck.manifest(root);
        Properties schemas = SchemaPinCheck.manifest(root);
        Properties formatting = FormattingPinCheck.manifest(root);
        require("1".equals(manifest.getProperty("schema"))
                        && integer(manifest, "source.count") == 226
                        && integer(manifest, "milestone.count") == 216
                        && integer(manifest, "data.count") == 132
                        && integer(manifest, "exceptional.count") == 84,
                "invalid fixed-wait migration manifest");
        String support = digest(root.resolve(
                "modules/smoketest/src/main/java/worldline/test/WorldlineSmokeAwait.java"));
        require(support.equals(manifest.getProperty("support.sha256"))
                        || support.equals(telemetry.getProperty("await_source.sha256")),
                "fixed-wait support drift");
        for (int index = 0; index < 226; index++) {
            String stem = "source." + index + "."; Path source = root.resolve(required(manifest,
                    stem + "path")).normalize();
            require(source.startsWith(root.resolve("smokes")) && Files.isRegularFile(source),
                    "missing migrated fixed-wait source: " + source);
            String text = Files.readString(source, StandardCharsets.UTF_8);
            require((digest(source).equals(required(manifest, stem + "current_sha256"))
                            || FormattingPinCheck.transportsFile(formatting, root,
                            required(manifest, stem + "path"),
                            required(manifest, stem + "current_sha256")))
                            && hash(manifest, stem + "prior_sha256")
                            && transition(required(manifest, stem + "path"),
                                    required(manifest, stem + "prior_sha256"),
                                    required(manifest, stem + "current_sha256")).equals(
                                            required(manifest, stem + "transition_sha256"))
                            && AWAIT.matcher(text).find() && !DEBT.matcher(text).find(),
                    "fixed-wait source evidence drift: " + root.relativize(source));
        }
        SmokePins pins = new SmokePins(root); String pinText = Files.readString(
                root.resolve("smokes/qualification.lock"), StandardCharsets.UTF_8);
        SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root);
        int checked = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String stem = "milestone." + smoke.id + ".";
            if (manifest.getProperty(stem + "current_fingerprint") == null) continue;
            checked++; String fingerprint = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, fingerprint);
            boolean direct = fingerprint.equals(required(manifest, stem + "current_fingerprint"))
                    && digest(root.resolve(smoke.runner)).equals(required(manifest, stem + "runner_sha256"))
                    && digest(root.resolve("smokes").resolve(smoke.id).resolve("smoke.properties"))
                    .equals(required(manifest, stem + "descriptor_sha256"))
                    && dataDescriptorChanged(root, smoke, manifest, stem);
            boolean instrumented = pin != null && TelemetryPinCheck.carries(
                    telemetry, smoke.id, pin, fingerprint)
                    && digest(root.resolve(smoke.runner)).equals(required(manifest, stem + "runner_sha256"))
                    && digest(root.resolve("smokes").resolve(smoke.id).resolve("smoke.properties"))
                    .equals(required(manifest, stem + "descriptor_sha256"))
                    && dataDescriptorChanged(root, smoke, manifest, stem);
            boolean schemaMigrated = pin != null && SchemaPinCheck.carries(
                    schemas, smoke.id, pin, fingerprint);
            boolean formatted = pin != null && FormattingPinCheck.carries(
                    formatting, smoke.id, pin, fingerprint);
            require(hash(manifest, stem + "prior_fingerprint")
                            && hash(manifest, stem + "prior_descriptor_sha256")
                            && hash(manifest, stem + "evidence_sha256")
                            && (direct || instrumented || schemaMigrated || formatted
                            || successor(dataDriven, composite, smoke.id, manifest, stem)),
                    "fixed-wait milestone evidence drift: " + smoke.id);
            require(pin != null && (pin.source().equals("executed")
                            || pin.source().equals("refactor-equivalent")
                            && (pin.evidence().equals(required(manifest, stem + "evidence_sha256"))
                            || TelemetryPinCheck.carries(telemetry, smoke.id, pin, fingerprint)
                            || SchemaPinCheck.carries(schemas, smoke.id, pin, fingerprint)
                            || FormattingPinCheck.carries(formatting, smoke.id, pin, fingerprint)))
                            && pinText.contains("# fixed-wait-refactor-proof="
                                    + "smokes/fixed-wait-migration.lock:milestone."
                                    + smoke.id + "\nsmoke." + smoke.id + ".fingerprint="),
                    "fixed-wait migration pin drift: " + smoke.id);
        }
        require(checked == 216, "fixed-wait milestone census drift: " + checked);
        System.out.println("  classified fixed waits: 226 sources; 216 milestones; raw debt=0");
    }

    private static boolean successor(Properties dataDriven, Properties composite, String id,
            Properties waits, String waitStem) {
        for (Properties migration : new Properties[] {dataDriven, composite}) {
            String stem = "cycle." + id + ".";
            if (migration.getProperty(stem + "source") == null) continue;
            return required(waits, waitStem + "runner").equals(migration.getProperty(stem + "source"))
                    && required(waits, waitStem + "runner_sha256").equals(
                    migration.getProperty(stem + "source_sha256"))
                    && required(waits, waitStem + "current_fingerprint").equals(
                    migration.getProperty(stem + "prior_fingerprint"))
                    && required(waits, waitStem + "evidence_sha256").equals(
                    migration.getProperty(stem + "evidence_sha256"));
        }
        return false;
    }

    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static boolean hash(Properties values, String key) {
        return values.getProperty(key, "").matches("[0-9a-f]{64}");
    }
    private static boolean dataDescriptorChanged(Path root, SmokeDiscovery.Entry smoke,
            Properties manifest, String stem) throws Exception {
        if (!smoke.runner.equals("tools/smoke/DataDrivenCycle.java")) return true;
        Properties descriptor = load(root.resolve("smokes").resolve(smoke.id)
                .resolve("smoke.properties"));
        return !required(manifest, stem + "prior_descriptor_sha256").equals(
                required(manifest, stem + "descriptor_sha256"))
                && !required(manifest, stem + "prior_descriptor_inputs").equals(
                        required(manifest, stem + "descriptor_inputs"))
                && required(manifest, stem + "descriptor_inputs").equals(
                        descriptor.getProperty("cycle.inputs", ""))
                && ("," + descriptor.getProperty("cycle.inputs", "") + ",")
                        .contains(",modules/smoketest/src/main/java,");
    }
    private static String required(Properties values, String key) { String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key); return value; }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readString(path, StandardCharsets.UTF_8)
                    .replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8))); }
    private static String transition(String path, String prior, String current) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(String.join("\0",
                "worldline-fixed-wait-transition-v1", path, prior, current)
                        .getBytes(StandardCharsets.UTF_8)));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
}
