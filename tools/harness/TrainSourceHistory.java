import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Preserves portable source-digest ancestry across repeated train reseals. */
final class TrainSourceHistory {
    private final Map<String, Set<String>> committed;
    private TrainSourceHistory(Map<String, Set<String>> committed) { this.committed = committed; }

    static TrainSourceHistory load(Path root) throws Exception {
        Map<String, Set<String>> values = new HashMap<>();
        for (String commit : capture(root, "log", "--format=%H", "--",
                "smokes/train-reconciliation.lock").lines().toList()) {
            Properties lock = new Properties();
            try (StringReader reader = new StringReader(capture(root, "show",
                    commit + ":smokes/train-reconciliation.lock"))) { lock.load(reader); }
            int count = integer(lock.getProperty("source.count", "0"));
            for (int index = 0; index < count; index++) {
                String stem = "source." + index + ".";
                Set<String> digests = values.computeIfAbsent(
                        lock.getProperty(stem + "path"), ignored -> new LinkedHashSet<>());
                add(digests, lock.getProperty(stem + "prior_sha256"));
                add(digests, lock.getProperty(stem + "current_sha256"));
                int ancestors = integer(lock.getProperty(stem + "ancestor.count", "0"));
                for (int item = 0; item < ancestors; item++)
                    add(digests, lock.getProperty(stem + "ancestor." + item + ".sha256"));
            }
        }
        return new TrainSourceHistory(Map.copyOf(values));
    }

    void write(Properties predecessor, Properties target, String stem, String relative) {
        String priorStem = find(predecessor, relative);
        Set<String> ancestors = new LinkedHashSet<>();
        ancestors.addAll(committed.getOrDefault(relative, Set.of()));
        if (priorStem != null) {
            add(ancestors, predecessor.getProperty(priorStem + "prior_sha256"));
            add(ancestors, predecessor.getProperty(priorStem + "current_sha256"));
            int count = integer(predecessor.getProperty(priorStem + "ancestor.count", "0"));
            for (int index = 0; index < count; index++)
                add(ancestors, predecessor.getProperty(priorStem + "ancestor." + index + ".sha256"));
        }
        target.setProperty(stem + "ancestor.count", Integer.toString(ancestors.size()));
        int index = 0;
        for (String digest : ancestors)
            target.setProperty(stem + "ancestor." + index++ + ".sha256", digest);
    }

    void writeSources(Path root, Properties target, Properties predecessor, String base)
            throws Exception {
        java.util.List<String> paths = capture(root, "diff", "--name-only", base, "--").lines()
                .filter(value -> !value.isBlank()
                        && !value.equals("smokes/qualification.lock")
                        && !value.equals("smokes/train-reconciliation.lock")
                        && !value.startsWith("smokes/qualification-evidence/"))
                .sorted().toList();
        target.setProperty("source.count", Integer.toString(paths.size())); int index = 0;
        for (String relative : paths) {
            String stem = "source." + index++ + "."; Path current = root.resolve(relative);
            String prior = show(root, base + ":" + relative);
            target.setProperty(stem + "path", relative);
            target.setProperty(stem + "prior_sha256", prior == null ? "added"
                    : digest(prior.getBytes(StandardCharsets.UTF_8)));
            target.setProperty(stem + "current_sha256", Files.isRegularFile(current)
                    ? digest(Files.readAllBytes(current)) : "removed");
            write(predecessor, target, stem, relative);
        }
    }

    static boolean connects(Properties lock, String stem, String digest) {
        int count = integer(lock.getProperty(stem + "ancestor.count", "0"));
        for (int index = 0; index < count; index++)
            if (digest.equals(lock.getProperty(stem + "ancestor." + index + ".sha256"))) return true;
        return false;
    }

    private static String find(Properties values, String relative) {
        int count = integer(values.getProperty("source.count", "0"));
        for (int index = 0; index < count; index++) {
            String stem = "source." + index + ".";
            if (relative.equals(values.getProperty(stem + "path"))) return stem;
        }
        return null;
    }

    private static void add(Set<String> values, String digest) {
        if (digest != null && (digest.equals("added") || digest.equals("removed")
                || digest.matches("[0-9a-f]{64}"))) values.add(digest);
    }

    private static int integer(String value) {
        try { int parsed = Integer.parseInt(value); return Math.max(0, parsed); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid train source history"); }
    }

    private static String capture(Path root, String... arguments) throws Exception {
        java.util.List<String> command = new java.util.ArrayList<>(java.util.List.of("git"));
        command.addAll(java.util.List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        if (process.waitFor() != 0) throw new IllegalStateException("git source history query failed");
        return output.toString(StandardCharsets.UTF_8);
    }

    private static String show(Path root, String object) throws Exception {
        Process process = new ProcessBuilder("git", "show", object).directory(root.toFile()).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        return process.waitFor() == 0 ? output.toString(StandardCharsets.UTF_8) : null;
    }

    private static String digest(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(PortableText.normalize(bytes)));
    }
}
