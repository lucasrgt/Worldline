import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Finds qualification graphs that consume one path, module, or configuration input. */
public final class SmokeInputDependentsQuery {
    private SmokeInputDependentsQuery() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 1) throw new IllegalArgumentException(
                    "usage: Gate.java --input-dependents KIND:KEY");
            Query query = Query.parse(arguments[0]);
            Path root = Path.of("").toAbsolutePath().normalize();
            SmokeInputFingerprint legacy = new SmokeInputFingerprint(root);
            SmokeInputGraph graphs = new SmokeInputGraph(root, legacy);
            List<Match> matches = new ArrayList<>();
            for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
                SmokeInputGraphResult graph = graphs.qualification(smoke);
                require(legacy.compute(smoke).equals(graph.rootSha256()),
                        "input graph root differs from legacy fingerprint: " + smoke.id);
                List<Integer> nodes = graph.nodes().stream().filter(query::matches)
                        .map(SmokeInputGraphResult.Node::index).toList();
                if (!nodes.isEmpty()) matches.add(new Match(smoke.id, nodes));
            }
            System.out.print(json(query, matches));
        } catch (Exception error) {
            System.err.println("smoke input dependents query failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void selfTest() {
        require(related("modules/api/src/A.java", "modules/api"), "file did not match parent node");
        require(related("modules/api", "modules/api/src"), "parent did not match child node");
        require(!related("modules/api", "modules/cli"), "unrelated paths matched");
        System.out.println("  smoke input dependents self-test: passed");
    }

    private static String json(Query query, List<Match> matches) {
        StringBuilder value = new StringBuilder("{\n  \"schema\":\"worldline.smoke-input-dependents.v1\",\n")
                .append("  \"kind\":\"").append(escape(query.kind)).append("\",\n")
                .append("  \"key\":\"").append(escape(query.key)).append("\",\n")
                .append("  \"impact\":\"").append(matches.isEmpty() ? "not-in-qualification-graph"
                        : "qualification").append("\",\n  \"count\":").append(matches.size())
                .append(",\n  \"smokes\":[\n");
        for (int index = 0; index < matches.size(); index++) {
            Match match = matches.get(index);
            value.append("    {\"id\":\"").append(match.id).append("\",\"nodes\":")
                    .append(match.nodes).append('}');
            if (index + 1 < matches.size()) value.append(',');
            value.append('\n');
        }
        return value.append("  ]\n}\n").toString();
    }

    private static boolean related(String left, String right) {
        return left.equals(right) || left.startsWith(right + "/") || right.startsWith(left + "/");
    }
    private static String escape(String value) { return SmokeInputGraphResult.escape(value); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private record Query(String kind, String key) {
        static Query parse(String raw) {
            int separator = raw.indexOf(':');
            require(separator > 0 && separator + 1 < raw.length(), "input query requires KIND:KEY");
            String kind = raw.substring(0, separator), key = raw.substring(separator + 1)
                    .replace('\\', '/');
            require(List.of("path", "module", "environment", "system-property").contains(kind),
                    "unsupported input query kind");
            require(key.matches("[A-Za-z0-9._/+-]+") && !key.startsWith("/")
                    && !key.contains("../"), "unsafe input query key");
            return new Query(kind, key);
        }
        boolean matches(SmokeInputGraphResult.Node node) {
            return node.kind().equals(kind) && (kind.equals("path")
                    ? related(key, node.key()) : key.equals(node.key()));
        }
    }
    private record Match(String id, List<Integer> nodes) { }
}
