import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Carries proofs across the reviewed telemetry-only smoke instrumentation change. */
final class TelemetryPinMigration {
    private final Path root;
    private TelemetryPinMigration(Path root) { this.root = root.toAbsolutePath().normalize(); }

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: TelemetryPinMigration --apply");
            new TelemetryPinMigration(Path.of("")).apply();
        } catch (Exception error) {
            System.err.println("telemetry pin migration failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void apply() throws Exception {
        Path lock = root.resolve("smokes/telemetry-migration.lock");
        Properties manifest = Files.isRegularFile(lock) ? load(lock) : new Properties();
        require(reviewed(manifest),
                "stage a reviewed change or commit the shared fingerprint change before migrating pins");
        SmokePins existing = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); SmokeReceiptCache cache =
                new SmokeReceiptCache(root); List<SmokePins.Entry> pins = new ArrayList<>();
        Properties providers = ProviderDiscoveryPinCheck.manifest(root);
        int changed = 0, carried = 0, executed = 0;
        manifest.setProperty("schema", "1");
        attest(manifest, "await_source", "modules/smoketest/src/main/java/worldline/test/WorldlineSmokeAwait.java");
        attest(manifest, "process_source", "tools/harness/SmokeProcess.java");
        attest(manifest, "execution_source", "tools/harness/SmokeExecution.java");
        attest(manifest, "history_source", "tools/harness/SmokeScheduleHistory.java");
        attest(manifest, "fingerprint_source", "tools/harness/SmokeInputFingerprint.java");
        attest(manifest, "policy", "quality/smoke-telemetry.properties");
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry prior = existing.entry(smoke.id);
            String current = fingerprints.compute(smoke);
            String stem = "smoke." + smoke.id + ".";
            SmokePins.Entry local = cache.availablePin(smoke);
            if (ProviderDiscoveryPinCheck.exemptsLegacy(providers, smoke.id)) {
                SmokePins.Entry proof = local != null ? local : prior;
                if (proof == null) proof = headEntry(smoke.id);
                require(proof != null, "new provider smoke lacks a prior proof: " + smoke.id);
                if (!current.equals(proof.fingerprint())) proof = new SmokePins.Entry(
                        smoke.id, current, proof.evidence(), "refactor-equivalent");
                pins.add(proof); remove(manifest, stem); continue;
            }
            if (local != null && local.source().equals("executed")) {
                pins.add(local); executed++;
                String recorded = manifest.getProperty(stem + "current_fingerprint");
                if (recorded != null) {
                    carried++;
                    if (!current.equals(recorded))
                        manifest.setProperty(stem + "prior_fingerprint", recorded);
                    manifest.setProperty(stem + "current_fingerprint", current);
                    manifest.setProperty(stem + "evidence_sha256", local.evidence());
                }
                continue;
            }
            require(prior != null, "missing prior smoke proof: " + smoke.id);
            if (current.equals(prior.fingerprint())) {
                pins.add(prior);
                String recorded = manifest.getProperty(stem + "current_fingerprint");
                if (recorded != null) {
                    carried++;
                    if (!current.equals(recorded))
                        manifest.setProperty(stem + "prior_fingerprint", recorded);
                    manifest.setProperty(stem + "current_fingerprint", current);
                    manifest.setProperty(stem + "evidence_sha256", prior.evidence());
                }
                continue;
            }
            changed++; carried++; manifest.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            manifest.setProperty(stem + "current_fingerprint", current);
            manifest.setProperty(stem + "evidence_sha256", prior.evidence());
            pins.add(new SmokePins.Entry(smoke.id, current, prior.evidence(), "refactor-equivalent"));
        }
        require(carried >= 100 && (executed >= 1 || changed == 0),
                "telemetry migration census drift: changed=" + changed + ";carried=" + carried
                        + ";executed=" + executed);
        manifest.setProperty("count", Integer.toString(
                carried + ProviderDiscoveryPinCheck.pendingCount(providers)));
        existing.write(pins);
        store(lock, manifest);
        System.out.println("telemetry pins migrated: " + changed + " changed, " + carried
                + " carried, " + executed + " exact support proofs");
    }

    private void attest(Properties manifest, String key, String relative) throws Exception {
        manifest.setProperty(key + ".path", relative);
        manifest.setProperty(key + ".sha256", digest(root.resolve(relative)));
    }
    private boolean dirtyIndex() throws Exception { Process process = new ProcessBuilder("git", "diff",
            "--cached", "--quiet").directory(root.toFile()).start(); return process.waitFor() == 1; }
    private boolean reviewed(Properties manifest) throws Exception {
        if (dirtyIndex()) return true;
        Process worktree = new ProcessBuilder("git", "diff", "--quiet")
                .directory(root.toFile()).start();
        Process index = new ProcessBuilder("git", "diff", "--cached", "--quiet")
                .directory(root.toFile()).start();
        String recorded = manifest.getProperty("fingerprint_source.sha256", "");
        return worktree.waitFor() == 0 && index.waitFor() == 0
                && !digest(root.resolve("tools/harness/SmokeInputFingerprint.java"))
                        .equals(recorded);
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values;
    }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline telemetry-only pin migration v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static void remove(Properties values, String stem) {
        values.remove(stem + "prior_fingerprint");
        values.remove(stem + "current_fingerprint");
        values.remove(stem + "evidence_sha256");
    }
    private SmokePins.Entry headEntry(String id) throws Exception {
        Process process = new ProcessBuilder("git", "show", "HEAD:smokes/qualification.lock")
                .directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor() == 0, "cannot read predecessor qualification lock");
        Properties values = new Properties(); values.load(new StringReader(output));
        String stem = "smoke." + id + ".", fingerprint = values.getProperty(stem + "fingerprint");
        return fingerprint == null ? null : new SmokePins.Entry(id, fingerprint,
                values.getProperty(stem + "observation_sha256"), values.getProperty(stem + "source"));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
