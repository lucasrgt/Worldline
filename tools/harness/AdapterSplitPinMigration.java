import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Seals the three adapter decompositions and carries their runtime observations. */
final class AdapterSplitPinMigration {
    private static final List<String> EXISTING = List.of(
            "adapters/b173-server/src/main/java/worldline/b173server/B173DedicatedServer.java",
            "adapters/b173-server/src/main/java/worldline/b173server/B173WireClient.java",
            "adapters/aero-model-lib/runtime-src/worldline/aero/mixin/WorldlineCaptureMixin.java");
    private static final List<String> ADDED = List.of(
            "adapters/b173-server/src/main/java/worldline/b173server/B173ServerProcess.java",
            "adapters/b173-server/src/main/java/worldline/b173server/B173WireLogin.java",
            "adapters/aero-model-lib/runtime-src/worldline/aero/mixin/WorldlineCaptureScene.java",
            "adapters/aero-model-lib/runtime-src/worldline/aero/mixin/WorldlineCaptureSettings.java");

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: AdapterSplitPinMigration --apply");
            apply(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("adapter-split pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void apply(Path root) throws Exception {
        Path lockPath = root.resolve("smokes/adapter-split.lock");
        Properties lock = Files.isRegularFile(lockPath) ? load(lockPath) : new Properties();
        if ("1".equals(lock.getProperty("schema"))
                && SmokeDiscovery.discover(root).size() > 525) {
            refresh(root, lockPath, lock); return;
        }
        lock.setProperty("schema", "1");
        lock.setProperty("existing.count", Integer.toString(EXISTING.size()));
        lock.setProperty("added.count", Integer.toString(ADDED.size()));
        int index = 0;
        for (String relative : EXISTING) {
            String stem = "existing." + index++ + ".";
            lock.setProperty(stem + "path", relative);
            lock.setProperty(stem + "prior_sha256", digest(git(root, "show", "HEAD:" + relative)));
            lock.setProperty(stem + "current_sha256", digest(Files.readString(root.resolve(relative))));
        }
        index = 0;
        for (String relative : ADDED) {
            require(git(root, "ls-files", "--error-unmatch", relative).strip().equals(relative),
                    "new adapter helper is not tracked: " + relative);
            String stem = "added." + index++ + ".";
            lock.setProperty(stem + "path", relative);
            lock.setProperty(stem + "current_sha256", digest(Files.readString(root.resolve(relative))));
        }
        SmokePins existing = new SmokePins(root); existing.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> pins = new ArrayList<>(); int changed = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry prior = existing.entry(smoke.id);
            require(prior != null, "adapter split lacks proof: " + smoke.id);
            String current = fingerprints.compute(smoke); String stem = "smoke." + smoke.id + ".";
            String original = lock.getProperty(stem + "prior_fingerprint", prior.fingerprint());
            if (!current.equals(original)) changed++;
            pins.add(current.equals(prior.fingerprint()) ? prior : new SmokePins.Entry(
                    smoke.id, current, prior.evidence(), "refactor-equivalent"));
            lock.setProperty(stem + "prior_fingerprint", original);
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", prior.evidence());
        }
        require(pins.size() == 525 && changed > 0, "adapter-split smoke census drift");
        lock.setProperty("smoke.count", Integer.toString(pins.size()));
        lock.setProperty("smoke.changed", Integer.toString(changed));
        existing.write(pins);
        store(lockPath, lock);
        System.out.println("adapter-split proofs: " + changed + " changed, 525 carried");
    }

    private static void refresh(Path root, Path path, Properties lock) throws Exception {
        Properties shared = SharedHelperPinCheck.manifest(root);
        int refactors = Integer.parseInt(shared.getProperty("refresh.count", "0"));
        require(refactors >= 1 && refactors <= 16,
                "adapter refresh requires shared-helper attestations");
        int sourceChanges = refreshSources(root, lock);
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties providers =
                ProviderDiscoveryPinCheck.manifest(root); int carried = 0, introduced = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (ProviderDiscoveryPinCheck.exemptsLegacy(providers, smoke.id)) continue;
            carried++; String current = fingerprints.compute(smoke); SmokePins.Entry pin =
                    pins.match(smoke.id, current); require(pin != null,
                    "adapter refresh lacks current proof: " + smoke.id);
            String stem = "smoke." + smoke.id + ".";
            String recorded = lock.getProperty(stem + "current_fingerprint");
            if (recorded == null) {
                require("executed".equals(pin.source()),
                        "new adapter row lacks exact execution: " + smoke.id);
                lock.setProperty(stem + "introduced", "true"); introduced++;
            } else if (!current.equals(recorded))
                lock.setProperty(stem + "prior_fingerprint", recorded);
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", pin.evidence());
        }
        int smokeCount = carried + ProviderDiscoveryPinCheck.pendingCount(providers);
        lock.setProperty("smoke.count", Integer.toString(smokeCount));
        pins.write(pins.entries()); store(path, lock);
        System.out.println("adapter-split pins refreshed: " + sourceChanges
                + " source changes, " + carried + " carried, " + introduced
                + " introduced");
    }

    private static int refreshSources(Path root, Properties lock) throws Exception {
        int changes = 0;
        for (String group : List.of("existing", "added")) {
            int count = Integer.parseInt(required(lock, group + ".count"));
            for (int index = 0; index < count; index++) {
                String stem = group + "." + index + ".";
                String relative = required(lock, stem + "path");
                String tracked = git(root, "ls-files", "--error-unmatch", relative).strip();
                require(tracked.equals(relative),
                        "adapter source is not tracked: " + relative);
                String prior = required(lock, stem + "current_sha256");
                String current = digest(Files.readString(root.resolve(relative)));
                if (current.equals(prior)) continue;
                int attestation = Integer.parseInt(
                        lock.getProperty("refresh.source.count", "0")) + 1;
                String record = "refresh.source." + attestation + ".";
                lock.setProperty(record + "path", relative);
                lock.setProperty(record + "prior_sha256", prior);
                lock.setProperty(record + "current_sha256", current);
                lock.setProperty("refresh.source.count", Integer.toString(attestation));
                lock.setProperty(stem + "current_sha256", current);
                changes++;
            }
        }
        return changes;
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command));
        return output.toString(StandardCharsets.UTF_8);
    }

    private static String digest(String text) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(
                text.replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8)));
    }

    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline adapter decomposition proof v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(),
                "missing " + key); return value;
    }
}
