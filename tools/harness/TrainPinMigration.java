import java.io.ByteArrayOutputStream;
import java.io.Reader;
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

/** Imports isolated milestone receipts and seals one content-addressed train. */
final class TrainPinMigration {
    private static final String BASE = "fd1e11d7c5e878d06137170e51b46aa9a5352569";
    private static final Set<String> PENDING = Set.of("gui-tree", "m7-mod-loading",
            "m8-mod-version-diff", "m9-scenario-minimization",
            "m620-stationapi-testkit-driver", "testkit-cycle");

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: TrainPinMigration --apply");
            Path root = Path.of("").toAbsolutePath().normalize();
            String configured = System.getenv("WORLDLINE_MILESTONE_WORKTREES");
            Path swarm = configured == null || configured.isBlank()
                    ? root.resolveSibling("worldline-swarm") : Path.of(configured);
            apply(root, swarm.toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("train pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void apply(Path root, Path swarm) throws Exception {
        require(Files.isDirectory(swarm), "missing milestone worktree root");
        require(status(root, "merge-base", "--is-ancestor", BASE, "HEAD") == 0,
                "train base is not an ancestor");
        Properties lock = new Properties(); lock.setProperty("schema", "1");
        lock.setProperty("base", BASE); sources(root, lock);
        Map<String, SmokePins.Entry> baseline = baseline(root);
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> updated = new ArrayList<>(); int imported = 0, carried = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String current = fingerprints.compute(smoke), stem = "smoke." + smoke.id + ".";
            SmokePins.Entry prior = baseline.get(smoke.id);
            lock.setProperty(stem + "current_fingerprint", current);
            if (PENDING.contains(smoke.id)) {
                lock.setProperty(stem + "kind", "pending");
                lock.setProperty(stem + "prior_fingerprint", prior == null ? "absent" : prior.fingerprint());
                lock.setProperty(stem + "evidence_sha256", prior == null ? "absent" : prior.evidence());
                if (prior != null) updated.add(prior);
                continue;
            }
            if (prior != null) {
                carried++; seal(lock, stem, "baseline", prior.fingerprint(), current, prior.evidence());
                updated.add(new SmokePins.Entry(smoke.id, current, prior.evidence(),
                        current.equals(prior.fingerprint()) ? prior.source() : "refactor-equivalent"));
                continue;
            }
            imported++; Imported receipt = imported(root, swarm, smoke.id);
            seal(lock, stem, "milestone", receipt.fingerprint, current, receipt.evidence);
            lock.setProperty(stem + "receipt.head", receipt.head);
            lock.setProperty(stem + "receipt.tree", receipt.tree);
            lock.setProperty(stem + "receipt.base", receipt.base);
            lock.setProperty(stem + "receipt.signature", receipt.signature);
            updated.add(new SmokePins.Entry(smoke.id, current, receipt.evidence,
                    "refactor-equivalent"));
        }
        int catalog = SmokeDiscovery.discover(root).size();
        require(imported > 0 && carried > 0 && updated.size() + 1 == catalog,
                "train proof census drift");
        lock.setProperty("catalog.count", Integer.toString(catalog));
        lock.setProperty("pin.count", Integer.toString(updated.size()));
        lock.setProperty("carried.count", Integer.toString(carried));
        lock.setProperty("imported.count", Integer.toString(imported));
        lock.setProperty("pending.count", Integer.toString(PENDING.size()));
        lock.setProperty("pending.smokes", String.join(",", PENDING.stream().sorted().toList()));
        pins.write(updated); store(root.resolve("smokes/train-reconciliation.lock"), lock);
        System.out.println("train proofs: " + carried + " carried, " + imported
                + " imported, " + PENDING.size() + " pending");
    }

    private static Imported imported(Path root, Path swarm, String id) throws Exception {
        Path worktree = swarm.resolve(id), report = worktree.resolve(
                ".worldline/reports/milestones").resolve(id + ".json");
        Path smokeReport = worktree.resolve(".worldline/reports/smokes").resolve(id + ".properties");
        Path log = worktree.resolve(".worldline/smoke-logs").resolve(id + ".log");
        require(Files.isRegularFile(report) && Files.isRegularFile(smokeReport)
                        && Files.isRegularFile(log), "missing milestone evidence: " + id);
        Map<String, Object> json = MiniJson.object(Files.readString(report, StandardCharsets.UTF_8));
        Properties attestation = load(smokeReport);
        String head = MiniJson.string(json, "head"), tree = MiniJson.string(json, "tree");
        String base = MiniJson.string(json, "base"), signature = MiniJson.string(json, "signature");
        String evidence = MiniJson.string(json, "evidence_sha256");
        require("passed".equals(MiniJson.string(json, "status"))
                        && head.equals(attestation.getProperty("head"))
                        && "passed".equals(attestation.getProperty("status"))
                        && evidence.equals(digest(Files.readAllBytes(log)))
                        && capture(worktree, "status", "--porcelain", "--untracked-files=all").isBlank()
                        && head.equals(capture(worktree, "rev-parse", "HEAD").strip()),
                "invalid milestone evidence: " + id);
        Properties source = load(worktree.resolve("smokes").resolve(id).resolve("smoke.properties"));
        Properties target = load(root.resolve("smokes").resolve(id).resolve("smoke.properties"));
        for (String key : List.of("expected.signal", "expected.signature", "cycle.main", "runner.source"))
            require(source.getProperty(key).equals(target.getProperty(key)),
                    "reconciled milestone behavior drift: " + id + " " + key);
        return new Imported(attestation.getProperty("fingerprint"), evidence,
                head, tree, base, signature);
    }

    private static void sources(Path root, Properties lock) throws Exception {
        List<String> paths = capture(root, "diff", "--name-only", BASE, "--").lines()
                .filter(value -> !value.isBlank()
                        && !value.equals("smokes/qualification.lock")
                        && !value.equals("smokes/train-reconciliation.lock")
                        && !value.startsWith("smokes/qualification-evidence/"))
                .sorted().toList();
        lock.setProperty("source.count", Integer.toString(paths.size())); int index = 0;
        for (String relative : paths) {
            String stem = "source." + index++ + "."; Path current = root.resolve(relative);
            String prior = show(root, BASE + ":" + relative);
            lock.setProperty(stem + "path", relative);
            lock.setProperty(stem + "prior_sha256", prior == null ? "added" : digest(prior));
            lock.setProperty(stem + "current_sha256", Files.isRegularFile(current)
                    ? digest(Files.readAllBytes(current)) : "removed");
        }
    }

    private static Map<String, SmokePins.Entry> baseline(Path root) throws Exception {
        Properties values = new Properties();
        try (StringReader reader = new StringReader(capture(root, "show",
                BASE + ":smokes/qualification.lock"))) { values.load(reader); }
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

    private static void seal(Properties lock, String stem, String kind,
            String prior, String current, String evidence) {
        lock.setProperty(stem + "kind", kind); lock.setProperty(stem + "prior_fingerprint", prior);
        lock.setProperty(stem + "current_fingerprint", current);
        lock.setProperty(stem + "evidence_sha256", evidence);
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values; }
    private static String show(Path root, String object) throws Exception {
        Process process = new ProcessBuilder("git", "show", object).directory(root.toFile()).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream(); process.getInputStream().transferTo(output);
        return process.waitFor() == 0 ? output.toString(StandardCharsets.UTF_8) : null;
    }
    private static String capture(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream(); process.getInputStream().transferTo(output);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command));
        return output.toString(StandardCharsets.UTF_8);
    }
    private static int status(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        return new ProcessBuilder(command).directory(root.toFile()).start().waitFor();
    }
    private static String digest(String text) throws Exception {
        return digest(text.replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8));
    }
    private static String digest(byte[] bytes) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(bytes)); }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline integration-train proof v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Imported(String fingerprint, String evidence, String head,
            String tree, String base, String signature) { }
}
