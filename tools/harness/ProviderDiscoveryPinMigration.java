import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Seals provider discovery and carries proofs that cannot observe the additive driver. */
final class ProviderDiscoveryPinMigration {
    private static final String NEW_SMOKE = "m620-stationapi-testkit-driver";
    private static final Set<String> PENDING = Set.of("m7-mod-loading", "m8-mod-version-diff",
            "m9-scenario-minimization", "testkit-cycle");
    private static final List<String> MODIFIED = List.of(
            "harness.properties",
            "modules/api/src/main/java/worldline/api/WorldlineContract.java",
            "modules/api/src/test/java/worldline/api/WorldlineContractTest.java",
            "modules/cli/src/main/java/worldline/cli/TestCommand.java",
            "modules/cli/src/test/java/worldline/cli/WorldlineCliTest.java",
            "modules/semantics/src/main/java/worldline/semantics/AdapterManifest.java",
            "modules/semantics/src/test/java/worldline/semantics/AdapterManifestTest.java",
            "tools/harness/AdapterKindCheck.java",
            "tools/harness/SmokeInputFingerprint.java");
    private static final List<String> ADDED = List.of(
            "modules/testapi/src/main/java/worldline/test/TestRuntimeProviders.java",
            "modules/testapi/src/test/java/worldline/testapi/TestRuntimeProvidersTest.java",
            "adapters/stationapi/semantics/manifest.properties",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiPlayer.java",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiProcess.java",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiProcesses.java",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiProtocol.java",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiRuntime.java",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiSettings.java",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiSnapshot.java",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiTestRuntimeProvider.java",
            "adapters/stationapi/src/main/java/worldline/stationapi/StationApiWorld.java");

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: ProviderDiscoveryPinMigration --apply");
            apply(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("provider-discovery pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void apply(Path root) throws Exception {
        Path lockPath = root.resolve("smokes/provider-discovery.lock");
        Properties lock = Files.isRegularFile(lockPath) ? load(lockPath) : new Properties();
        if ("1".equals(lock.getProperty("schema"))
                && SmokeDiscovery.discover(root).size() > 526) {
            refresh(root, lockPath, lock); return;
        }
        lock.setProperty("schema", "1"); lock.setProperty("new.smoke", NEW_SMOKE);
        sources(root, lock, "modified", MODIFIED, true);
        sources(root, lock, "added", ADDED, false);
        SmokePins existing = new SmokePins(root); existing.validateEvidence();
        Map<String, SmokePins.Entry> baseline = baseline(root);
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> pins = new ArrayList<>(); int changed = 0, discovered = 0, carried = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            discovered++;
            if (smoke.id.equals(NEW_SMOKE)) continue;
            SmokePins.Entry prior = baseline.get(smoke.id);
            require(prior != null, "provider discovery lacks prior proof: " + smoke.id);
            String current = fingerprints.compute(smoke); String stem = "smoke." + smoke.id + ".";
            if (PENDING.contains(smoke.id)) {
                pins.add(prior); lock.setProperty(stem + "prior_fingerprint", prior.fingerprint());
                lock.setProperty(stem + "current_fingerprint", current);
                lock.setProperty(stem + "evidence_sha256", prior.evidence()); continue;
            }
            carried++;
            String original = lock.getProperty(stem + "prior_fingerprint", prior.fingerprint());
            if (!current.equals(prior.fingerprint())) changed++;
            pins.add(current.equals(prior.fingerprint()) ? prior : new SmokePins.Entry(
                    smoke.id, current, prior.evidence(), "refactor-equivalent"));
            lock.setProperty(stem + "prior_fingerprint", original);
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", prior.evidence());
        }
        require(discovered == 526 && pins.size() == 525 && carried == 521 && changed > 0,
                "provider-discovery smoke census drift");
        lock.setProperty("smoke.count", Integer.toString(carried));
        lock.setProperty("catalog.count", Integer.toString(discovered));
        lock.setProperty("pending.count", Integer.toString(PENDING.size()));
        lock.setProperty("pending.smokes", String.join(",", PENDING.stream().sorted().toList()));
        lock.setProperty("smoke.changed", Integer.toString(changed));
        existing.write(pins); store(lockPath, lock);
        System.out.println("provider-discovery proofs: " + changed
                + " changed, 521 carried, 4 runtime-pending");
    }

    private static void refresh(Path root, Path path, Properties lock) throws Exception {
        Properties shared = SharedHelperPinCheck.manifest(root);
        require(Integer.parseInt(shared.getProperty("refresh.count", "0")) >= 1,
                "provider refresh requires shared-helper attestations");
        int sourceChanges = refreshSources(root, lock);
        Properties train = TrainPinCheck.manifest(root); SmokePins pins = new SmokePins(root);
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        List<SmokePins.Entry> nextPins = new ArrayList<>(pins.entries());
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        int discovered = 0, carried = 0, introduced = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (TrainPinCheck.isAdded(train, smoke.id)) continue;
            discovered++;
            if (smoke.id.equals(NEW_SMOKE)) continue;
            if (PENDING.contains(smoke.id)) {
                String current = fingerprints.compute(smoke);
                SmokePins.Entry matched = pins.match(smoke.id, current);
                SmokePins.Entry stored = pins.entry(smoke.id);
                String stem = "smoke." + smoke.id + ".";
                boolean valid = matched != null && "executed".equals(matched.source())
                        || matched == null && stored != null
                        && stored.fingerprint().equals(lock.getProperty(stem + "prior_fingerprint"))
                        && stored.evidence().equals(lock.getProperty(stem + "evidence_sha256"));
                if (!valid && !TrainPinCheck.isPending(train, smoke.id)) {
                    SmokePins.Entry exact = cache.availablePin(smoke);
                    require(exact != null && "executed".equals(exact.source())
                                    && current.equals(exact.fingerprint()),
                            "resolved provider pending smoke lacks exact current execution: " + smoke.id);
                    replace(nextPins, exact);
                }
                continue;
            }
            carried++; String current = fingerprints.compute(smoke); SmokePins.Entry pin =
                    pins.match(smoke.id, current); require(pin != null,
                    "provider refresh lacks current proof: " + smoke.id);
            String stem = "smoke." + smoke.id + ".";
            String recorded = lock.getProperty(stem + "current_fingerprint");
            if (recorded == null) {
                require("executed".equals(pin.source()),
                        "new provider row lacks exact execution: " + smoke.id);
                lock.setProperty(stem + "introduced", "true"); introduced++;
            } else if (!current.equals(recorded))
                lock.setProperty(stem + "prior_fingerprint", recorded);
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", pin.evidence());
        }
        int catalog = Integer.parseInt(lock.getProperty("catalog.count")) + introduced;
        int smokeCount = Integer.parseInt(lock.getProperty("smoke.count")) + introduced;
        require(discovered == catalog && carried == smokeCount, "provider refresh census drift");
        lock.setProperty("catalog.count", Integer.toString(catalog));
        lock.setProperty("smoke.count", Integer.toString(smokeCount));
        pins.write(nextPins); store(path, lock);
        System.out.println("provider-discovery pins refreshed: " + smokeCount + " carried, "
                + introduced + " introduced, " + sourceChanges + " source changes");
    }

    private static int refreshSources(Path root, Properties lock) throws Exception {
        int priorChanges = Integer.parseInt(lock.getProperty("refresh.source.count", "0"));
        int changes = 0; Properties train = TrainPinCheck.manifest(root);
        for (String group : List.of("modified", "added")) {
            int count = Integer.parseInt(required(lock, group + ".count"));
            for (int index = 0; index < count; index++) {
                String stem = group + "." + index + ".";
                String current = digest(Files.readString(root.resolve(required(lock, stem + "path"))));
                String prior = required(lock, stem + "current_sha256");
                if (current.equals(prior)) continue;
                String relative = required(lock, stem + "path");
                require(TrainPinCheck.transportsFile(train, root, relative, prior)
                                || fingerprintExtension(root, relative),
                        "provider source change lacks reviewed transport: " + relative);
                String refresh = "refresh.source." + (priorChanges + ++changes) + ".";
                lock.setProperty(refresh + "path", relative);
                lock.setProperty(refresh + "prior_sha256", prior);
                lock.setProperty(refresh + "current_sha256", current);
                lock.setProperty(stem + "current_sha256", current);
            }
        }
        lock.setProperty("refresh.source.count", Integer.toString(priorChanges + changes));
        return changes;
    }

    private static void replace(List<SmokePins.Entry> pins, SmokePins.Entry exact) {
        for (int index = 0; index < pins.size(); index++)
            if (pins.get(index).id().equals(exact.id())) { pins.set(index, exact); return; }
        pins.add(exact);
    }

    private static boolean fingerprintExtension(Path root, String relative) throws Exception {
        if (!relative.equals("tools/harness/SmokeInputFingerprint.java")) return false;
        String fingerprint = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
        String plan = Files.readString(root.resolve("tools/harness/DataDrivenCyclePlan.java"),
                StandardCharsets.UTF_8);
        String boundary = "src/(?:main|testkit)/java";
        return fingerprint.contains(boundary) && plan.contains(boundary);
    }

    private static Map<String, SmokePins.Entry> baseline(Path root) throws Exception {
        Properties values = new Properties();
        try (var reader = new java.io.StringReader(git(root, "show", "HEAD:smokes/qualification.lock"))) {
            values.load(reader);
        }
        java.util.HashMap<String, SmokePins.Entry> result = new java.util.HashMap<>();
        for (String key : values.stringPropertyNames()) {
            if (!key.startsWith("smoke.") || !key.endsWith(".fingerprint")) continue;
            String id = key.substring(6, key.length() - 12), stem = "smoke." + id + ".";
            result.put(id, new SmokePins.Entry(id, values.getProperty(key),
                    values.getProperty(stem + "observation_sha256"),
                    values.getProperty(stem + "evidence_sha256"), values.getProperty(stem + "source")));
        }
        return Map.copyOf(result);
    }

    private static void sources(Path root, Properties lock, String group, List<String> paths,
            boolean prior) throws Exception {
        lock.setProperty(group + ".count", Integer.toString(paths.size())); int index = 0;
        for (String relative : paths) {
            require(git(root, "ls-files", "--error-unmatch", relative).strip().equals(relative),
                    "provider-discovery source is not tracked: " + relative);
            String stem = group + "." + index++ + "."; lock.setProperty(stem + "path", relative);
            if (prior) lock.setProperty(stem + "prior_sha256",
                    digest(git(root, "show", "HEAD:" + relative)));
            lock.setProperty(stem + "current_sha256", digest(Files.readString(root.resolve(relative))));
        }
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command));
        return output.toString(StandardCharsets.UTF_8);
    }
    private static String digest(String text) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(text.replace("\r\n", "\n")
                    .getBytes(StandardCharsets.UTF_8))); }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline provider-discovery proof v1\n");
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
