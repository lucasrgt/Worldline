import java.nio.file.Path;

/** Canonical --smoke suite extracted so Verify can grow milestones without a packed file. */
final class SmokeSuite {
    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/SmokeSuite.java");
            System.exit(2);
        }
        try { run(); }
        catch (Exception error) {
            System.err.println("smoke suite failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private SmokeSuite() {}

    static void run() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        java.util.List<SmokeDiscovery.Entry> smokes = SmokeDiscovery.discover(root);
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        for (SmokeDiscovery.Entry smoke : smokes) {
            String fingerprint = cache.fingerprint(smoke);
            if (!cache.restore(smoke, fingerprint))
                cache.passed(smoke, fingerprint, SmokeExecution.run(root, smoke));
        }
        cache.finish(smokes.size());
    }
}
