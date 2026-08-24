import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Discovers self-contained smoke milestones without a shared source catalog. */
final class SmokeDiscovery {
    private SmokeDiscovery() {}

    static List<Entry> discover(Path root) throws IOException {
        Path smokeRoot = root.resolve("smokes");
        Path runnerRoot = root.resolve("tools/smoke");
        List<Path> runners = javaFiles(runnerRoot);
        List<Draft> drafts = new ArrayList<>();
        try (Stream<Path> stream = Files.list(smokeRoot)) {
            for (Path directory : stream.filter(Files::isDirectory).sorted().collect(Collectors.toList())) {
                String id = directory.getFileName().toString();
                Path manifest = directory.resolve("smoke.properties");
                if (!Files.isRegularFile(manifest)) continue;
                Properties descriptor = loadIfPresent(manifest);
                String declared = descriptor.getProperty("id", id).trim();
                require(id.equals(declared), "smoke directory/id mismatch: " + id + " != " + declared);
                drafts.add(new Draft(id, configuredRunner(root, runnerRoot, descriptor)));
            }
        }
        Map<String, List<Path>> inferred = inferRunners(runners, drafts);
        List<Entry> entries = new ArrayList<>();
        for (Draft draft : drafts) {
            Path runner = draft.runner;
            if (runner == null) runner = requireInferred(root, draft.id, inferred.get(draft.id));
            entries.add(new Entry(draft.id, root.relativize(runner).toString().replace('\\', '/')));
        }
        entries.sort(Comparator.comparingInt((Entry entry) -> ordinal(entry.id))
                .thenComparing(entry -> entry.id));
        validate(entries, runners.size());
        return entries;
    }

    static Entry require(Path root, String id) throws IOException {
        return discover(root).stream().filter(entry -> entry.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("unknown smoke milestone: " + id));
    }

    private static Path configuredRunner(Path root, Path runnerRoot, Properties descriptor) {
        String configured = descriptor.getProperty("runner.source");
        if (configured == null || configured.isBlank()) return null;
        Path runner = root.resolve(configured).toAbsolutePath().normalize();
        require(runner.startsWith(runnerRoot.toAbsolutePath().normalize()) && Files.isRegularFile(runner),
                "invalid runner.source: " + configured);
        return runner;
    }

    private static Map<String, List<Path>> inferRunners(List<Path> runners, List<Draft> drafts)
            throws IOException {
        Map<String, List<Path>> matches = new HashMap<>();
        for (Draft draft : drafts) if (draft.runner == null) matches.put(draft.id, new ArrayList<>());
        for (Path runner : runners) {
            String source = Files.readString(runner, StandardCharsets.UTF_8);
            for (Map.Entry<String, List<Path>> candidate : matches.entrySet())
                if (source.contains("\"" + candidate.getKey() + "\"")) candidate.getValue().add(runner);
        }
        return matches;
    }

    private static Path requireInferred(Path root, String id, List<Path> matches) {
        if (matches == null) matches = List.of();
        require(matches.size() == 1, "smoke " + id + " has " + matches.size()
                + " inferred runners; add runner.source to its descriptor: "
                + matches.stream().map(root::relativize).collect(Collectors.toList()));
        return matches.get(0);
    }

    private static Properties loadIfPresent(Path path) throws IOException {
        return Files.isRegularFile(path) ? StrictProperties.load(path) : new Properties();
    }

    private static List<Path> javaFiles(Path root) throws IOException {
        try (Stream<Path> stream = Files.list(root)) {
            return stream.filter(path -> path.toString().endsWith(".java"))
                    .sorted().collect(Collectors.toList());
        }
    }

    private static int ordinal(String id) {
        if (id.equals("deterministic-world-tick")) return -3;
        if (id.equals("controlled-client-tick")) return -2;
        if (id.equals("testkit-cycle")) return -1;
        if (!id.startsWith("m")) return Integer.MAX_VALUE;
        int end = 1;
        while (end < id.length() && Character.isDigit(id.charAt(end))) end++;
        try { return Integer.parseInt(id.substring(1, end)); }
        catch (NumberFormatException error) { return Integer.MAX_VALUE - 1; }
    }

    private static void validate(List<Entry> entries, int runnerCount) {
        Set<String> ids = new HashSet<>(), runners = new HashSet<>();
        for (Entry entry : entries) {
            require(ids.add(entry.id), "duplicate smoke id: " + entry.id);
            runners.add(entry.runner);
        }
        require(runners.size() == runnerCount, "unregistered smoke runners: discovered "
                + runners.size() + " of " + runnerCount);
    }

    static final class Entry {
        final String id, runner;
        Entry(String id, String runner) { this.id = id; this.runner = runner; }
    }

    private static final class Draft {
        final String id; final Path runner;
        Draft(String id, Path runner) { this.id = id; this.runner = runner; }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
