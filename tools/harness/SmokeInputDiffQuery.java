import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Compares one current qualification graph with the same graph at a Git commit. */
public final class SmokeInputDiffQuery {
    private SmokeInputDiffQuery() { }

    public static void main(String[] arguments) {
        try {
            Spec spec = Spec.parse(arguments);
            Path root = Path.of("").toAbsolutePath().normalize();
            SmokeInputGraphResult after = graph(root, null, spec.smoke);
            try (GitSourceSnapshot snapshot = GitSourceSnapshot.open(root, spec.reference)) {
                SmokeInputGraphResult before = graph(snapshot.root(), snapshot.tracked(), spec.smoke);
                SmokeGitState state = SmokeGitState.read(root);
                System.out.print(json(snapshot.commit(), state, before, after, differences(before, after)));
            }
        } catch (Exception error) {
            System.err.println("smoke input diff query failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void selfTest() {
        String one = "1".repeat(64), two = "2".repeat(64), three = "3".repeat(64);
        SmokeInputGraphResult before = new SmokeInputGraphResult("fixture", one, List.of(
                new SmokeInputGraphResult.Node(0, "path", "a", one),
                new SmokeInputGraphResult.Node(1, "path", "removed", two)));
        SmokeInputGraphResult after = new SmokeInputGraphResult("fixture", two, List.of(
                new SmokeInputGraphResult.Node(0, "path", "a", three),
                new SmokeInputGraphResult.Node(1, "module", "added", one)));
        List<Change> changes = differences(before, after);
        require(changes.size() == 3 && changes.stream().map(Change::status).collect(
                java.util.stream.Collectors.toSet()).equals(Set.of("changed", "removed", "added")),
                "input graph diff classification drifted");
        System.out.println("  smoke input diff self-test: passed");
    }

    private static SmokeInputGraphResult graph(Path root, Set<Path> tracked, String id)
            throws Exception {
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        SmokeInputFingerprint legacy = tracked == null ? new SmokeInputFingerprint(root)
                : new SmokeInputFingerprint(root, tracked);
        SmokeInputGraphResult graph = new SmokeInputGraph(root, legacy).qualification(smoke);
        require(legacy.compute(smoke).equals(graph.rootSha256()),
                "input graph root differs from legacy fingerprint: " + id);
        return graph;
    }

    private static List<Change> differences(SmokeInputGraphResult before,
            SmokeInputGraphResult after) {
        Map<Key, SmokeInputGraphResult.Node> left = index(before.nodes()), right = index(after.nodes());
        Set<Key> keys = new LinkedHashSet<>(left.keySet()); keys.addAll(right.keySet());
        List<Change> changes = new ArrayList<>();
        for (Key key : keys) {
            SmokeInputGraphResult.Node old = left.get(key), current = right.get(key);
            if (old == null) changes.add(new Change(key.kind, key.key, key.occurrence,
                    "added", null, current.sha256()));
            else if (current == null) changes.add(new Change(key.kind, key.key, key.occurrence,
                    "removed", old.sha256(), null));
            else if (!old.sha256().equals(current.sha256())) changes.add(new Change(
                    key.kind, key.key, key.occurrence, "changed", old.sha256(), current.sha256()));
        }
        return List.copyOf(changes);
    }

    private static Map<Key, SmokeInputGraphResult.Node> index(List<SmokeInputGraphResult.Node> nodes) {
        Map<Key, SmokeInputGraphResult.Node> result = new LinkedHashMap<>();
        Map<String, Integer> occurrences = new HashMap<>();
        for (SmokeInputGraphResult.Node node : nodes) {
            String identity = node.kind() + "\0" + node.key();
            int occurrence = occurrences.merge(identity, 1, Integer::sum) - 1;
            result.put(new Key(node.kind(), node.key(), occurrence), node);
        }
        return result;
    }

    private static String json(String commit, SmokeGitState state, SmokeInputGraphResult before,
            SmokeInputGraphResult after, List<Change> changes) {
        StringBuilder value = new StringBuilder("{\n  \"schema\":\"worldline.smoke-input-diff.v1\",\n")
                .append("  \"smoke\":\"").append(escape(after.smoke())).append("\",\n")
                .append("  \"before_commit\":\"").append(commit).append("\",\n")
                .append("  \"after_head\":\"").append(state.head()).append("\",\n")
                .append("  \"after_clean\":").append(state.clean()).append(",\n")
                .append("  \"before_root_sha256\":\"").append(before.rootSha256()).append("\",\n")
                .append("  \"after_root_sha256\":\"").append(after.rootSha256()).append("\",\n")
                .append("  \"changed_count\":").append(changes.size()).append(",\n  \"changes\":[\n");
        for (int index = 0; index < changes.size(); index++) {
            Change change = changes.get(index);
            value.append("    {\"kind\":\"").append(escape(change.kind)).append("\",\"key\":\"")
                    .append(escape(change.key)).append("\",\"occurrence\":").append(change.occurrence)
                    .append(",\"status\":\"").append(change.status).append("\",\"before_sha256\":")
                    .append(nullable(change.before)).append(",\"after_sha256\":")
                    .append(nullable(change.after)).append('}');
            if (index + 1 < changes.size()) value.append(','); value.append('\n');
        }
        return value.append("  ]\n}\n").toString();
    }

    private static String nullable(String value) { return value == null ? "null" : "\"" + value + "\""; }
    private static String escape(String value) { return SmokeInputGraphResult.escape(value); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private record Spec(String smoke, String reference) {
        static Spec parse(String[] arguments) {
            require(arguments.length == 1, "usage: Gate.java --input-diff SMOKE_ID@GIT_REF");
            int separator = arguments[0].lastIndexOf('@');
            String smoke = separator < 0 ? "" : arguments[0].substring(0, separator);
            String reference = separator < 0 ? "" : arguments[0].substring(separator + 1);
            require(smoke.matches("[a-z0-9]+(?:-[a-z0-9]+)*") && !reference.isBlank(),
                    "input diff requires SMOKE_ID@GIT_REF");
            return new Spec(smoke, reference);
        }
    }
    private record Key(String kind, String key, int occurrence) { }
    private record Change(String kind, String key, int occurrence,
            String status, String before, String after) { }
}
