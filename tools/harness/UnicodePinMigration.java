import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/** Carries proofs across the CRLF/Unicode-NFC portable-text identity upgrade. */
final class UnicodePinMigration {
    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: UnicodePinMigration --apply");
            apply(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("Unicode pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void apply(Path root) throws Exception {
        Path path = root.resolve("smokes/unicode-normalization.lock");
        if (Files.isRegularFile(path)) {
            Properties lock = load(path);
            if ("1".equals(lock.getProperty("schema"))) { refresh(root, path, lock); return; }
        }
        SmokePins existing = new SmokePins(root); existing.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> pins = new ArrayList<>(); Properties lock = new Properties();
        lock.setProperty("schema", "1"); lock.setProperty("normalization", "utf8-lf-nfc");
        int changed = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry prior = existing.entry(smoke.id);
            require(prior != null, "Unicode migration lacks proof: " + smoke.id);
            String current = fingerprints.compute(smoke), stem = "smoke." + smoke.id + ".";
            if (!current.equals(prior.fingerprint())) changed++;
            pins.add(current.equals(prior.fingerprint()) ? prior : new SmokePins.Entry(
                    smoke.id, current, prior.evidence(), "refactor-equivalent"));
            lock.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", prior.evidence());
        }
        require(pins.size() == 525 && changed > 0, "Unicode migration census drift");
        lock.setProperty("smoke.count", "525"); lock.setProperty("smoke.changed", Integer.toString(changed));
        existing.write(pins); store(path, lock);
        System.out.println("Unicode-normalized proofs: " + changed + " changed, 525 carried");
    }
    private static void refresh(Path root, Path path, Properties lock) throws Exception {
        Properties shared = SharedHelperPinCheck.manifest(root);
        int refactors = Integer.parseInt(shared.getProperty("refresh.count", "0"));
        require(refactors >= 1 && refactors <= 16,
                "Unicode refresh requires shared-helper attestations");
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties providers =
                ProviderDiscoveryPinCheck.manifest(root); int carried = 0, introduced = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (ProviderDiscoveryPinCheck.exemptsLegacy(providers, smoke.id)) continue;
            carried++; String current = fingerprints.compute(smoke); SmokePins.Entry pin =
                    pins.match(smoke.id, current); require(pin != null,
                    "Unicode refresh lacks current proof: " + smoke.id);
            String stem = "smoke." + smoke.id + ".";
            String recorded = lock.getProperty(stem + "current_fingerprint");
            if (recorded == null) {
                require("executed".equals(pin.source()),
                        "new Unicode row lacks exact execution: " + smoke.id);
                lock.setProperty(stem + "introduced", "true"); introduced++;
            } else if (!current.equals(recorded))
                lock.setProperty(stem + "prior_fingerprint", recorded);
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", pin.evidence());
        }
        Properties schemas = SchemaPinCheck.manifest(root);
        int successorIntroductions = SchemaPinCheck.introductionsAfter(schemas, lock);
        int smokeCount = carried + ProviderDiscoveryPinCheck.pendingCount(providers)
                - successorIntroductions;
        require(smokeCount >= 0, "Unicode successor introduction census drift");
        lock.setProperty("smoke.count", Integer.toString(smokeCount));
        pins.write(pins.entries()); store(path, lock);
        System.out.println("Unicode-normalized pins refreshed: " + carried + " carried, "
                + introduced + " introduced");
    }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline portable UTF-8 normalization v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values;
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(),
                "missing " + key); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
