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
        for (String commit : capture(root, "log", "--all", "--format=%H", "--",
                "smokes/train-reconciliation.lock").lines().toList()) {
            Properties lock = new Properties();
            try (StringReader reader = new StringReader(capture(root, "show",
                    commit + ":smokes/train-reconciliation.lock"))) { lock.load(reader); }
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

    record Receipt(String prior, String head, String tree, String base, String signature) { }
    private static String capture(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(); command.add("git");
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream(); process.getInputStream().transferTo(output);
        if (process.waitFor() != 0) throw new IllegalStateException("git history query failed");
        return output.toString(StandardCharsets.UTF_8);
    }
}
