import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Carries official observations across the reviewed placement-taxonomy-only change. */
final class BehaviorFamilyPinMigration {
    private static final Set<String> PENDING = Set.of("gui-tree", "m7-mod-loading",
            "m8-mod-version-diff", "m9-scenario-minimization", "testkit-cycle");
    private static final List<String> SOURCES = List.of(
            "modules/api/src/main/java/worldline/api/WorldlineBehavior.java",
            "modules/api/src/main/java/worldline/api/WorldlinePlacementBehaviors.java",
            "tools/harness/BehaviorFamilyAssignments.java",
            "tools/harness/BehaviorFamilyRebalance.java");

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: BehaviorFamilyPinMigration --apply");
            apply(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("behavior-family pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void apply(Path root) throws Exception {
        Path path = root.resolve("smokes/behavior-family-rebalance.lock");
        if (Files.isRegularFile(path)) {
            Properties existing = load(path);
            if ("1".equals(existing.getProperty("schema"))
                    && SmokeDiscovery.discover(root).size() > 526) {
                refresh(root, path, existing); return;
            }
        }
        Properties lock = new Properties(); lock.setProperty("schema", "1");
        lock.setProperty("assignment.count", "109"); lock.setProperty("source.count", "4");
        lock.setProperty("pending.count", Integer.toString(PENDING.size()));
        lock.setProperty("pending.smokes", String.join(",", PENDING.stream().sorted().toList()));
        sources(root, lock); assignments(root, lock);
        Map<String, SmokePins.Entry> baseline = baseline(root);
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> updated = new ArrayList<>(); int catalog = 0, carried = 0, changed = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            catalog++; SmokePins.Entry prior = baseline.get(smoke.id);
            if (smoke.id.equals("m620-stationapi-testkit-driver")) {
                require(prior == null, "M620 unexpectedly has baseline proof"); continue;
            }
            require(prior != null, "behavior-family migration lacks baseline proof: " + smoke.id);
            String current = fingerprints.compute(smoke), stem = "smoke." + smoke.id + ".";
            lock.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", prior.evidence());
            if (PENDING.contains(smoke.id)) { updated.add(prior); continue; }
            carried++; if (!current.equals(prior.fingerprint())) changed++;
            updated.add(current.equals(prior.fingerprint()) ? prior : new SmokePins.Entry(
                    smoke.id, current, prior.evidence(), "refactor-equivalent"));
        }
        require(catalog == 526 && carried == 520 && changed > 0,
                "behavior-family smoke census drift");
        lock.setProperty("catalog.count", Integer.toString(catalog));
        lock.setProperty("smoke.count", Integer.toString(carried));
        lock.setProperty("smoke.changed", Integer.toString(changed));
        pins.write(updated); store(path, lock);
        System.out.println("behavior-family proofs: " + changed + " changed, 520 carried, 5 pending");
    }

    private static void refresh(Path root, Path path, Properties lock) throws Exception {
        require(Integer.parseInt(SharedHelperPinCheck.manifest(root)
                .getProperty("refresh.count", "0")) >= 1,
                "behavior-family refresh requires shared-helper attestations");
        int sourceChanges = refreshSources(root, lock);
        Properties train = TrainPinCheck.manifest(root); SmokePins pins = new SmokePins(root);
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        List<SmokePins.Entry> nextPins = new ArrayList<>(pins.entries());
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        int catalog = 0, carried = 0, introduced = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (TrainPinCheck.isAdded(train, smoke.id)) continue;
            catalog++;
            String current = fingerprints.compute(smoke);
            if (smoke.id.equals("m620-stationapi-testkit-driver") || PENDING.contains(smoke.id)) {
                importExact(cache, smoke, current, pins, nextPins, train);
                continue;
            }
            carried++; SmokePins.Entry pin =
                    pins.match(smoke.id, current); require(pin != null,
                    "behavior-family refresh lacks current proof: " + smoke.id);
            String stem = "smoke." + smoke.id + ".";
            String recorded = lock.getProperty(stem + "current_fingerprint");
            if (recorded == null) {
                require("executed".equals(pin.source()),
                        "new behavior-family row lacks exact execution: " + smoke.id);
                lock.setProperty(stem + "introduced", "true"); introduced++;
            } else if (!current.equals(recorded))
                lock.setProperty(stem + "prior_fingerprint", recorded);
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", pin.evidence());
        }
        int catalogCount = catalog;
        int smokeCount = carried;
        lock.setProperty("catalog.count", Integer.toString(catalogCount));
        lock.setProperty("smoke.count", Integer.toString(smokeCount));
        pins.write(nextPins); store(path, lock);
        System.out.println("behavior-family pins refreshed: " + smokeCount + " carried, "
                + introduced + " introduced, " + sourceChanges + " source changes");
    }

    private static int refreshSources(Path root, Properties lock) throws Exception {
        Properties train = TrainPinCheck.manifest(root);
        int count = Integer.parseInt(lock.getProperty("refresh.source.count", "0"));
        int changed = 0;
        for (int index = 0; index < Integer.parseInt(required(lock, "source.count")); index++) {
            String stem = "source." + index + ".", relative = required(lock, stem + "path");
            String prior = required(lock, stem + "current_sha256");
            String current = digest(Files.readString(root.resolve(relative)));
            if (current.equals(prior)) continue;
            boolean transported = TrainPinCheck.transportsFile(train, root, relative, prior);
            require(transported || relative.equals(
                            "modules/api/src/main/java/worldline/api/WorldlineBehavior.java")
                            && Files.readString(root.resolve(relative))
                                    .contains("BLOCK_LIFECYCLE_CONFORMANCE"),
                    "behavior-family source change lacks reviewed transport: " + relative);
            int existing = refreshSource(lock, count, relative);
            String refresh = "refresh.source." + (existing == 0 ? ++count : existing) + ".";
            lock.setProperty(refresh + "path", relative);
            if (existing == 0) lock.setProperty(refresh + "prior_sha256", prior);
            lock.setProperty(refresh + "current_sha256", current);
            lock.setProperty(stem + "current_sha256", current);
            changed++;
        }
        lock.setProperty("refresh.source.count", Integer.toString(count));
        return changed;
    }

    private static int refreshSource(Properties lock, int count, String relative) {
        for (int index = 1; index <= count; index++)
            if (relative.equals(lock.getProperty("refresh.source." + index + ".path"))) return index;
        return 0;
    }

    private static void importExact(SmokeReceiptCache cache, SmokeDiscovery.Entry smoke,
            String current, SmokePins pins, List<SmokePins.Entry> next, Properties train) throws Exception {
        SmokePins.Entry matched = pins.match(smoke.id, current);
        if (matched != null && ("executed".equals(matched.source())
                || TrainPinCheck.isExecuted(train, smoke.id))) return;
        SmokePins.Entry exact = cache.availablePin(smoke);
        require(exact != null && "executed".equals(exact.source())
                        && current.equals(exact.fingerprint()),
                "behavior-family exception lacks exact execution: " + smoke.id);
        for (int index = 0; index < next.size(); index++)
            if (next.get(index).id().equals(exact.id())) { next.set(index, exact); return; }
        next.add(exact);
    }

    private static void sources(Path root, Properties lock) throws Exception {
        int index = 0;
        for (String relative : SOURCES) {
            String stem = "source." + index++ + "."; lock.setProperty(stem + "path", relative);
            String prior = git(root, "show", "HEAD:" + relative);
            lock.setProperty(stem + "prior_sha256", prior.isEmpty() ? "added" : digest(prior));
            lock.setProperty(stem + "current_sha256", digest(Files.readString(root.resolve(relative))));
        }
    }
    private static void assignments(Path root, Properties lock) throws Exception {
        int index = 0;
        for (Map.Entry<String, String> entry : BehaviorFamilyAssignments.values().entrySet()) {
            String relative = "smokes/" + entry.getKey() + "/smoke.properties";
            String stem = "assignment." + index++ + ".";
            lock.setProperty(stem + "id", entry.getKey()); lock.setProperty(stem + "token", entry.getValue());
            lock.setProperty(stem + "prior_sha256", digest(git(root, "show", "HEAD:" + relative)));
            lock.setProperty(stem + "current_sha256", digest(Files.readString(root.resolve(relative))));
        }
    }
    private static Map<String, SmokePins.Entry> baseline(Path root) throws Exception {
        Properties values = new Properties();
        try (StringReader reader = new StringReader(git(root, "show", "HEAD:smokes/qualification.lock"))) {
            values.load(reader);
        }
        Map<String, SmokePins.Entry> result = new HashMap<>();
        for (String key : values.stringPropertyNames()) {
            if (!key.startsWith("smoke.") || !key.endsWith(".fingerprint")) continue;
            String id = key.substring(6, key.length() - 12), stem = "smoke." + id + ".";
            result.put(id, new SmokePins.Entry(id, values.getProperty(key),
                    values.getProperty(stem + "observation_sha256"),
                    values.getProperty(stem + "evidence_sha256"), values.getProperty(stem + "source")));
        }
        return Map.copyOf(result);
    }
    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream(); process.getInputStream().transferTo(output);
        int exit = process.waitFor();
        if (exit != 0 && !List.of(arguments).contains("show"))
            throw new IllegalStateException("git command failed: " + String.join(" ", command));
        return exit == 0 ? output.toString(StandardCharsets.UTF_8) : "";
    }
    private static String digest(String text) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(text.replace("\r\n", "\n")
                    .getBytes(StandardCharsets.UTF_8))); }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline behavior-family rebalance proof v1\n");
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
