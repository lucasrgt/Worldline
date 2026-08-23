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
        existing.write(pins); store(root.resolve("smokes/unicode-normalization.lock"), lock);
        System.out.println("Unicode-normalized proofs: " + changed + " changed, 525 carried");
    }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline portable UTF-8 normalization v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
