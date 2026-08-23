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

/** Seals the additive workbench GUI contract while leaving changed runtime proofs pending. */
final class GuiWorkbenchPinMigration {
    private static final Set<String> PENDING = Set.of("gui-tree", "m7-mod-loading",
            "m8-mod-version-diff", "m9-scenario-minimization", "testkit-cycle");
    private static final List<String> MODIFIED = List.of(
            "modules/api/src/main/java/worldline/api/GameUiNode.java",
            "modules/api/src/main/java/worldline/api/GameUiSpec.java",
            "modules/api/src/test/java/worldline/api/DomainApiTest.java",
            "adapters/b173-client/src/main/java/worldline/b173/B173Gui.java",
            "smokes/gui-tree/src/worldline/smoke/gui/GuiTreeSmoke.java",
            "smokes/gui-tree/oracle-src/GuiTreeOracle.java",
            "smokes/gui-tree/smoke.properties", "smokes/gui-tree/MAP.md",
            "tools/smoke/GuiCycle.java");
    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: GuiWorkbenchPinMigration --apply");
            apply(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("GUI workbench pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }
    private static void apply(Path root) throws Exception {
        Path lockPath = root.resolve("smokes/gui-workbench.lock");
        Properties lock = Files.isRegularFile(lockPath) ? load(lockPath) : new Properties();
        lock.setProperty("schema", "1");
        lock.setProperty("pending.smokes", String.join(",", PENDING.stream().sorted().toList()));
        lock.setProperty("pending.count", Integer.toString(PENDING.size()));
        lock.setProperty("additional.pending", "gui-tree"); sources(root, lock);
        Properties release = load(root.resolve("release/worldline.properties"));
        Properties descriptor = load(root.resolve("smokes/gui-tree/smoke.properties"));
        lock.setProperty("release.status", "runtime-pending");
        lock.setProperty("release.prior_signature", release.getProperty("gui.signature"));
        lock.setProperty("release.current_signature", descriptor.getProperty("expected.signature"));
        SmokePins existing = new SmokePins(root); existing.validateEvidence();
        Map<String, SmokePins.Entry> baseline = baseline(root);
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> pins = new ArrayList<>(); int changed = 0, carried = 0, catalog = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            catalog++; SmokePins.Entry prior = baseline.get(smoke.id);
            if (smoke.id.equals("m620-stationapi-testkit-driver")) {
                require(prior == null, "M620 unexpectedly has baseline proof"); continue;
            }
            require(prior != null, "GUI workbench lacks baseline proof: " + smoke.id);
            String current = fingerprints.compute(smoke), stem = "smoke." + smoke.id + ".";
            lock.putIfAbsent(stem + "prior_fingerprint", prior.fingerprint());
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", prior.evidence());
            if (PENDING.contains(smoke.id)) { pins.add(prior); continue; }
            carried++; if (!current.equals(lock.getProperty(stem + "prior_fingerprint"))) changed++;
            pins.add(current.equals(prior.fingerprint()) ? prior : new SmokePins.Entry(
                    smoke.id, current, prior.evidence(), "refactor-equivalent"));
        }
        require(catalog == 526 && pins.size() == 525 && carried == 520 && changed > 0,
                "GUI workbench smoke census drift");
        lock.setProperty("catalog.count", Integer.toString(catalog));
        lock.setProperty("smoke.count", Integer.toString(carried));
        lock.setProperty("smoke.changed", Integer.toString(changed));
        existing.write(pins); store(lockPath, lock);
        System.out.println("GUI workbench proofs: " + changed + " changed, 520 carried, 5 pending");
    }
    private static void sources(Path root, Properties lock) throws Exception {
        lock.setProperty("source.count", Integer.toString(MODIFIED.size())); int index = 0;
        for (String relative : MODIFIED) {
            String stem = "source." + index++ + "."; lock.setProperty(stem + "path", relative);
            lock.putIfAbsent(stem + "prior_sha256", digest(git(root, "show", "HEAD:" + relative)));
            lock.setProperty(stem + "current_sha256", digest(Files.readString(root.resolve(relative))));
        }
    }
    private static Map<String, SmokePins.Entry> baseline(Path root) throws Exception {
        Properties values = new Properties();
        try (StringReader reader = new StringReader(git(root, "show", "HEAD:smokes/qualification.lock"))) {
            values.load(reader); }
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
        StringBuilder output = new StringBuilder("# Worldline workbench GUI proof boundary v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
