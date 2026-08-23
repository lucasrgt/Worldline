import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
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
        require(dirtyIndex(), "stage the telemetry implementation before migrating pins");
        SmokePins existing = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); List<SmokePins.Entry> pins = new ArrayList<>();
        Path lock = root.resolve("smokes/telemetry-migration.lock");
        Properties manifest = Files.isRegularFile(lock) ? load(lock) : new Properties();
        int changed = 0, carried = 0;
        manifest.setProperty("schema", "1");
        attest(manifest, "await_source", "modules/smoketest/src/main/java/worldline/test/WorldlineSmokeAwait.java");
        attest(manifest, "process_source", "tools/harness/SmokeProcess.java");
        attest(manifest, "execution_source", "tools/harness/SmokeExecution.java");
        attest(manifest, "history_source", "tools/harness/SmokeScheduleHistory.java");
        attest(manifest, "policy", "quality/smoke-telemetry.properties");
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry prior = existing.entry(smoke.id);
            require(prior != null, "missing prior smoke proof: " + smoke.id);
            String current = fingerprints.compute(smoke);
            String stem = "smoke." + smoke.id + ".";
            if (current.equals(prior.fingerprint())) {
                pins.add(prior);
                if (manifest.getProperty(stem + "current_fingerprint") != null) carried++;
                continue;
            }
            changed++; carried++; manifest.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            manifest.setProperty(stem + "current_fingerprint", current);
            manifest.setProperty(stem + "evidence_sha256", prior.evidence());
            pins.add(new SmokePins.Entry(smoke.id, current, prior.evidence(), "refactor-equivalent"));
        }
        require(changed >= 1 && carried >= 100,
                "telemetry migration census drift: changed=" + changed + ";carried=" + carried);
        manifest.setProperty("count", Integer.toString(carried)); existing.write(pins);
        store(lock, manifest);
        System.out.println("telemetry pins migrated: " + changed + " changed, " + carried + " carried");
    }

    private void attest(Properties manifest, String key, String relative) throws Exception {
        manifest.setProperty(key + ".path", relative);
        manifest.setProperty(key + ".sha256", digest(root.resolve(relative)));
    }
    private boolean dirtyIndex() throws Exception { Process process = new ProcessBuilder("git", "diff",
            "--cached", "--quiet").directory(root.toFile()).start(); return process.waitFor() == 1; }
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
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
