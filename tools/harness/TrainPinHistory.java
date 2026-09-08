import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Collapses evidence-equivalent train edges to the latest independently locked anchor. */
final class TrainPinHistory {
    private final List<Properties> locks;
    private final Set<String> anchors;
    private TrainPinHistory(List<Properties> locks, Set<String> anchors) {
        this.locks = locks; this.anchors = anchors;
    }
    static TrainPinHistory load(Path root) throws Exception {
        List<Properties> history = new ArrayList<>();
        List<String> commits = capture(root, "log", "--all", "--format=%H", "--",
                "smokes/train-reconciliation.lock").lines().filter(value -> !value.isBlank()).toList();
        List<String> objects = commits.stream().map(commit ->
                commit + ":smokes/train-reconciliation.lock").toList();
        for (String content : GitObjectBatch.read(root, objects, false)) {
            Properties lock = new Properties();
            try (StringReader reader = new StringReader(content)) { lock.load(reader); }
            history.add(lock);
        }
        Set<String> anchors = new HashSet<>();
        try (var paths = Files.list(root.resolve("smokes"))) {
            for (Path path : paths.filter(value -> value.toString().endsWith(".lock"))
                    .filter(value -> !value.getFileName().toString().equals(
                            "train-reconciliation.lock")).toList()) {
                Properties lock = new Properties();
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    lock.load(reader);
                }
                for (String key : lock.stringPropertyNames()) if (key.endsWith(".current_fingerprint"))
                    anchors.add(lock.getProperty(key));
            }
        }
        return new TrainPinHistory(List.copyOf(history), Set.copyOf(anchors));
    }
    String inherited(String stem, String evidence, String prior) {
        Set<String> seen = new HashSet<>();
        while (!anchors.contains(prior) && seen.add(prior)) {
            String next = prior;
            for (Properties lock : locks) if (evidence.equals(lock.getProperty(stem + "evidence_sha256"))
                    && prior.equals(lock.getProperty(stem + "current_fingerprint"))) {
                next = lock.getProperty(stem + "prior_fingerprint", prior);
                if (!next.equals(prior)) break;
            }
            if (next.equals(prior)) break;
            prior = next;
        }
        return prior;
    }

    Receipt receipt(String stem, String evidence) {
        for (Properties lock : locks) {
            if (!evidence.equals(lock.getProperty(stem + "evidence_sha256"))) continue;
            String head = lock.getProperty(stem + "receipt.head");
            String tree = lock.getProperty(stem + "receipt.tree");
            String base = lock.getProperty(stem + "receipt.base");
            String signature = lock.getProperty(stem + "receipt.signature");
            if (head != null && tree != null && base != null && signature != null)
                return new Receipt(lock.getProperty(stem + "prior_fingerprint"),
                        head, tree, base, signature);
        }
        return null;
    }

    static void preserveUnchangedOrigins(Properties next, Properties previous) {
        for (String key : previous.stringPropertyNames()) {
            if (!key.startsWith("smoke.") || !key.endsWith(".prior_fingerprint")) continue;
            String stem = key.substring(0, key.length() - "prior_fingerprint".length());
            String fingerprint = previous.getProperty(stem + "current_fingerprint");
            String evidence = previous.getProperty(stem + "evidence_sha256");
            if (fingerprint != null && evidence != null
                    && fingerprint.equals(next.getProperty(stem + "current_fingerprint"))
                    && evidence.equals(next.getProperty(stem + "evidence_sha256"))) {
                next.setProperty(key, previous.getProperty(key));
            }
        }
    }
    static void selfTest() {
        Properties previous = new Properties(), next = new Properties();
        for (String id : List.of("entity-persistence", "wall-sign", "torch", "modloader-forge")) {
            String stem = "smoke." + id + ".";
            previous.setProperty(stem + "prior_fingerprint", "origin");
            previous.setProperty(stem + "current_fingerprint", "current");
            previous.setProperty(stem + "evidence_sha256", "proof");
        }
        next.putAll(previous);
        for (String key : previous.stringPropertyNames()) if (key.endsWith(".prior_fingerprint"))
            next.setProperty(key, "current");
        preserveUnchangedOrigins(next, previous);
        if (!next.equals(previous)) throw new IllegalStateException("unchanged proof origin drift");
        next.setProperty("smoke.torch.current_fingerprint", "changed");
        next.setProperty("smoke.torch.prior_fingerprint", "new-origin");
        next.setProperty("smoke.wall-sign.evidence_sha256", "new-proof");
        next.setProperty("smoke.wall-sign.prior_fingerprint", "new-origin");
        preserveUnchangedOrigins(next, previous);
        if (!"new-origin".equals(next.getProperty("smoke.torch.prior_fingerprint"))
                || !"new-origin".equals(next.getProperty("smoke.wall-sign.prior_fingerprint"))) {
            throw new IllegalStateException("changed proof origin overwritten");
        }
    }
    record Receipt(String prior, String head, String tree, String base, String signature) { }

    static int preserveUnchanged(Path root, Properties target) throws Exception {
        Properties parent = new Properties();
        try (StringReader reader = new StringReader(capture(root, "show",
                "HEAD^:smokes/train-reconciliation.lock"))) { parent.load(reader); }
        return preserveUnchanged(parent, target);
    }

    static int preserveUnchanged(Properties predecessor, Properties target) {
        Set<String> keys = new HashSet<>(predecessor.stringPropertyNames());
        keys.addAll(target.stringPropertyNames()); int preserved = 0;
        for (String key : target.stringPropertyNames()) {
            if (!key.startsWith("smoke.") || !key.endsWith(".prior_fingerprint")) continue;
            String prior = predecessor.getProperty(key);
            if (prior == null || prior.equals(target.getProperty(key))) continue;
            String stem = key.substring(0, key.length() - "prior_fingerprint".length());
            boolean unchanged = keys.stream().filter(item -> item.startsWith(stem) && !item.equals(key))
                    .allMatch(item -> java.util.Objects.equals(
                            predecessor.getProperty(item), target.getProperty(item)));
            if (unchanged) { target.setProperty(key, prior); preserved++; }
        }
        return preserved;
    }

    static void selfTest() {
        Properties predecessor = new Properties(); String stem = "smoke.fixture.";
        predecessor.setProperty(stem + "prior_fingerprint", "old");
        predecessor.setProperty(stem + "current_fingerprint", "current");
        predecessor.setProperty(stem + "evidence_sha256", "evidence");
        predecessor.setProperty(stem + "kind", "baseline");
        Properties target = new Properties(); target.putAll(predecessor);
        target.setProperty(stem + "prior_fingerprint", "current");
        if (preserveUnchanged(predecessor, target) != 1
                || !"old".equals(target.getProperty(stem + "prior_fingerprint"))
                || preserveUnchanged(predecessor, target) != 0)
            throw new IllegalStateException("unchanged train history was not preserved idempotently");
        for (String field : List.of("current_fingerprint", "evidence_sha256", "kind", "receipt.head")) {
            target.clear(); target.putAll(predecessor);
            target.setProperty(stem + "prior_fingerprint", "current");
            target.setProperty(stem + field, "changed");
            if (preserveUnchanged(predecessor, target) != 0)
                throw new IllegalStateException("changed train proof inherited an unrelated predecessor");
        }
        target.clear(); target.putAll(predecessor);
        target.setProperty(stem + "prior_fingerprint", "current");
        target.remove(stem + "kind");
        if (preserveUnchanged(predecessor, target) != 0)
            throw new IllegalStateException("incomplete train proof inherited a predecessor");
    }
    private static String capture(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(); command.add("git");
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream(); process.getInputStream().transferTo(output);
        if (process.waitFor() != 0) throw new IllegalStateException("git history query failed");
        return output.toString(StandardCharsets.UTF_8);
    }
}
