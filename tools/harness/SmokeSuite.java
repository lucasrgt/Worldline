import java.nio.file.Path;

/** Canonical --smoke suite extracted so Verify can grow milestones without a packed file. */
final class SmokeSuite {
    public static void main(String[] arguments) {
        boolean pinnedOnly = java.util.Arrays.equals(arguments, new String[] {"--pinned-only"});
        if (arguments.length != 0 && !pinnedOnly) {
            System.err.println("usage: java tools/harness/SmokeSuite.java [--pinned-only]");
            System.exit(2);
        }
        try { run(pinnedOnly); }
        catch (Exception error) {
            System.err.println("smoke suite failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private SmokeSuite() {}

    static void run() throws Exception { run(false); }

    static void run(boolean pinnedOnly) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        java.util.List<SmokeDiscovery.Entry> smokes = SmokeDiscovery.discover(root);
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        for (SmokeDiscovery.Entry smoke : smokes) {
            String fingerprint = cache.fingerprint(smoke);
            boolean restored = cache.restore(smoke, fingerprint);
            if (pinnedOnly && !restored)
                throw new IllegalStateException("pinned smoke proof missing: " + smoke.id);
            if (!restored)
                cache.passed(smoke, fingerprint, SmokeExecution.run(root, smoke));
        }
        cache.finish(smokes.size());
    }
}
