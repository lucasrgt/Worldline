import java.nio.file.Path;

/** Prints a non-mutating explanation of one smoke cache decision. */
public final class SmokeCacheQuery {
    private SmokeCacheQuery() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 1) throw new IllegalArgumentException(
                    "usage: Gate.java --cache-explain SMOKE_ID");
            Path root = Path.of("").toAbsolutePath().normalize();
            SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, arguments[0]);
            System.out.print(new SmokeReceiptCache(root).explain(smoke).json());
        } catch (Exception error) {
            System.err.println("smoke cache query failed: " + error.getMessage());
            System.exit(1);
        }
    }
}
