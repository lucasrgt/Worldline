import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Preserves portable source-digest ancestry across repeated train reseals. */
final class TrainSourceHistory {
    private final Map<String, Set<String>> committed;
    private final Map<String, String> previous;
    private TrainSourceHistory(Map<String, Set<String>> committed) {
        this(committed, Map.of());
    }
    private TrainSourceHistory(Map<String, Set<String>> committed, Map<String, String> previous) {
        this.committed = committed; this.previous = previous;
    }

    static TrainSourceHistory load(Path root) throws Exception {
        Map<String, Set<String>> values = new HashMap<>();
        List<String> commits = capture(root, "log", "--format=%H", "--",
                "smokes/train-reconciliation.lock").lines().filter(value -> !value.isBlank()).toList();
        List<String> objects = commits.stream().map(commit ->
                commit + ":smokes/train-reconciliation.lock").toList();
        List<String> contents = GitObjectBatch.read(root, objects, false);
        for (String content : contents) {
            Properties lock = new Properties();
            try (StringReader reader = new StringReader(content)) { lock.load(reader); }
            int count = integer(lock.getProperty("source.count", "0"));
            for (int index = 0; index < count; index++) {
                String stem = "source." + index + ".";
                Set<String> digests = values.computeIfAbsent(
                        lock.getProperty(stem + "path"), ignored -> new LinkedHashSet<>());
                add(digests, lock.getProperty(stem + "current_sha256"));
            }
        }
        for (String content : contents) {
            Properties lock = new Properties();
            try (StringReader reader = new StringReader(content)) { lock.load(reader); }
            int count = integer(lock.getProperty("source.count", "0"));
            for (int index = 0; index < count; index++) {
                String stem = "source." + index + ".";
                Set<String> digests = values.computeIfAbsent(
                        lock.getProperty(stem + "path"), ignored -> new LinkedHashSet<>());
                int ancestors = integer(lock.getProperty(stem + "ancestor.count", "0"));
                for (int item = 0; item < ancestors; item++)
                    add(digests, lock.getProperty(stem + "ancestor." + item + ".sha256"));
                add(digests, lock.getProperty(stem + "prior_sha256"));
            }
        }
        Map<String, String> previous = contents.size() < 2 ? Map.of() : currents(contents.get(1));
        return new TrainSourceHistory(Map.copyOf(values), previous);
    }

    void write(Properties predecessor, Properties target, String stem, String relative) {
        String priorStem = find(predecessor, relative);
        if (priorStem != null && java.util.Objects.equals(
                predecessor.getProperty(priorStem + "current_sha256"),
                target.getProperty(stem + "current_sha256"))) {
            writeAncestors(target, stem, orderedRecorded(predecessor, priorStem, relative));
            return;
        }
        Set<String> ancestors = new LinkedHashSet<>();
        if (priorStem != null) {
            add(ancestors, predecessor.getProperty(priorStem + "current_sha256"));
            ancestors.addAll(orderedRecorded(predecessor, priorStem, relative));
            add(ancestors, predecessor.getProperty(priorStem + "prior_sha256"));
        }
        for (String digest : committed.getOrDefault(relative, Set.of()))
            if (!digest.equals(target.getProperty(stem + "current_sha256"))) add(ancestors, digest);
        writeAncestors(target, stem, ancestors);
    }

    private Set<String> orderedRecorded(Properties source, String sourceStem, String relative) {
        Set<String> recorded = new LinkedHashSet<>();
        int count = integer(source.getProperty(sourceStem + "ancestor.count", "0"));
        for (int index = 0; index < count; index++)
            add(recorded, source.getProperty(sourceStem + "ancestor." + index + ".sha256"));
        boolean changedInPriorLock = !java.util.Objects.equals(
                source.getProperty(sourceStem + "current_sha256"), previous.get(relative));
        if (!changedInPriorLock || recorded.isEmpty() || !java.util.List.of("added", "removed")
                    .contains(recorded.iterator().next())) return recorded;
        String predecessor = committed.getOrDefault(relative, Set.of()).stream()
                .filter(value -> value.matches("[0-9a-f]{64}") && recorded.contains(value))
                .findFirst().orElse(null);
        if (predecessor == null) return recorded;
        Set<String> ordered = new LinkedHashSet<>(); ordered.add(predecessor);
        ordered.addAll(recorded); return ordered;
    }

    private static void writeAncestors(Properties target, String stem, Set<String> ancestors) {
        target.setProperty(stem + "ancestor.count", Integer.toString(ancestors.size()));
        int index = 0;
        for (String digest : ancestors)
            target.setProperty(stem + "ancestor." + index++ + ".sha256", digest);
    }

    private static Map<String, String> currents(String content) throws Exception {
        Properties lock = new Properties();
        try (StringReader reader = new StringReader(content)) { lock.load(reader); }
        Map<String, String> values = new HashMap<>();
        int count = integer(lock.getProperty("source.count", "0"));
        for (int index = 0; index < count; index++) {
            String stem = "source." + index + ".";
            String path = lock.getProperty(stem + "path");
            String digest = lock.getProperty(stem + "current_sha256");
            if (path != null && digest != null) values.put(path, digest);
        }
        return Map.copyOf(values);
    }

    void writeSources(Path root, Properties target, Properties predecessor,
            Properties ordering, String base)
            throws Exception {
        java.util.List<String> discovered = capture(root, "diff", "--name-only", base, "--").lines()
                .filter(value -> !value.isBlank()
                        && !value.equals("smokes/qualification.lock")
                        && !value.equals("smokes/train-reconciliation.lock")
                        && !value.startsWith("smokes/qualification-evidence/"))
                .sorted().toList();
        java.util.List<String> paths = stablePaths(ordering, discovered);
        List<String> priorSources = GitObjectBatch.read(root, paths.stream().map(relative ->
                base + ":" + relative).toList(), true);
        target.setProperty("source.count", Integer.toString(paths.size())); int index = 0;
        for (String relative : paths) {
            String stem = "source." + index++ + "."; Path current = root.resolve(relative);
            String prior = priorSources.get(index - 1);
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

    static void selfTest() {
        Properties prior = new Properties(); prior.setProperty("source.count", "3");
        prior.setProperty("source.0.path", "z"); prior.setProperty("source.1.path", "removed");
        prior.setProperty("source.2.path", "a");
        require(stablePaths(prior, java.util.List.of("a", "new", "z"))
                .equals(java.util.List.of("z", "a", "new")), "train sources were renumbered");
        String digest = "a".repeat(64); prior = new Properties();
        prior.setProperty("source.count", "1"); prior.setProperty("source.0.path", "same");
        prior.setProperty("source.0.current_sha256", digest);
        prior.setProperty("source.0.ancestor.count", "1");
        prior.setProperty("source.0.ancestor.0.sha256", "b".repeat(64));
        Properties target = new Properties();
        target.setProperty("source.0.current_sha256", digest);
        new TrainSourceHistory(Map.of("same", Set.of("c".repeat(64))))
                .write(prior, target, "source.0.", "same");
        require("1".equals(target.getProperty("source.0.ancestor.count"))
                        && "b".repeat(64).equals(
                                target.getProperty("source.0.ancestor.0.sha256")),
                "unchanged train source ancestry advanced");
        prior.setProperty("source.0.prior_sha256", "added");
        target.setProperty("source.0.current_sha256", "d".repeat(64));
        Set<String> committed = new LinkedHashSet<>(java.util.List.of(
                digest, "c".repeat(64), "added"));
        new TrainSourceHistory(Map.of("same", committed))
                .write(prior, target, "source.0.", "same");
        require("4".equals(target.getProperty("source.0.ancestor.count"))
                        && digest.equals(target.getProperty("source.0.ancestor.0.sha256"))
                        && "b".repeat(64).equals(target.getProperty("source.0.ancestor.1.sha256"))
                        && "added".equals(target.getProperty("source.0.ancestor.2.sha256"))
                        && "c".repeat(64).equals(target.getProperty("source.0.ancestor.3.sha256")),
                "changed train source did not preserve predecessor-first ancestry");
        prior.setProperty("source.0.current_sha256", "d".repeat(64));
        prior.setProperty("source.0.ancestor.0.sha256", "added");
        prior.setProperty("source.0.ancestor.1.sha256", digest);
        prior.setProperty("source.0.ancestor.count", "2");
        target = new Properties(); target.setProperty("source.0.current_sha256", "d".repeat(64));
        new TrainSourceHistory(Map.of("same", committed), Map.of("same", digest))
                .write(prior, target, "source.0.", "same");
        require(digest.equals(target.getProperty("source.0.ancestor.0.sha256"))
                        && "added".equals(target.getProperty("source.0.ancestor.1.sha256")),
                "recent sentinel-first ancestry was not repaired narrowly");
    }

    private static java.util.List<String> stablePaths(
            Properties predecessor, java.util.List<String> discovered) {
        Set<String> remaining = new LinkedHashSet<>(discovered);
        java.util.List<String> ordered = new java.util.ArrayList<>();
        int count = integer(predecessor.getProperty("source.count", "0"));
        for (int index = 0; index < count; index++) {
            String path = predecessor.getProperty("source." + index + ".path");
            if (remaining.remove(path)) ordered.add(path);
        }
        ordered.addAll(remaining); return java.util.List.copyOf(ordered);
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

    private static String digest(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(HexDigest.sha256()
                .digest(PortableText.normalize(bytes)));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
