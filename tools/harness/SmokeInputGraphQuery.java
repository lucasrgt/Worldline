import java.nio.file.Path;

/** Emits one secret-free qualification input graph as canonical JSON. */
public final class SmokeInputGraphQuery {
    private SmokeInputGraphQuery() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 1 || !arguments[0].matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
                throw new IllegalArgumentException("usage: Gate.java --input-graph SMOKE_ID");
            Path root = Path.of("").toAbsolutePath().normalize();
            SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, arguments[0]);
            SmokeInputFingerprint legacy = new SmokeInputFingerprint(root);
            SmokeInputGraphResult graph = new SmokeInputGraph(root, legacy).qualification(smoke);
            if (!legacy.compute(smoke).equals(graph.rootSha256()))
                throw new IllegalStateException("input graph root differs from legacy fingerprint");
            System.out.print(graph.json());
        } catch (Exception error) {
            System.err.println("smoke input graph query failed: " + error.getMessage());
            System.exit(1);
        }
    }
}
