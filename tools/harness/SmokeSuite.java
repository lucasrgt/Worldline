import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        List<SmokeDiscovery.Entry> smokes = SmokeDiscovery.discover(root);
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        Map<String, String> fingerprints = new HashMap<>();
        List<SmokeDiscovery.Entry> misses = new ArrayList<>();
        for (SmokeDiscovery.Entry smoke : smokes) {
            String fingerprint = cache.fingerprint(smoke);
            fingerprints.put(smoke.id, fingerprint);
            if (cache.restorable(smoke, fingerprint)) continue;
            if (pinnedOnly)
                throw new IllegalStateException("pinned smoke proof missing: " + smoke.id);
            misses.add(smoke);
        }
        Map<String, Long> durations = execute(root, smokes, misses);
        for (SmokeDiscovery.Entry smoke : smokes) {
            String fingerprint = fingerprints.get(smoke.id);
            Long duration = durations.get(smoke.id);
            if (duration != null) cache.passed(smoke, fingerprint, duration);
            else if (!cache.restore(smoke, fingerprint))
                throw new IllegalStateException("smoke proof was not restorable: " + smoke.id);
        }
        cache.finish(smokes.size());
    }

    /** Runs independent misses through a bounded pool; chained work stays strictly serial. */
    private static Map<String, Long> execute(Path root, List<SmokeDiscovery.Entry> catalog,
            List<SmokeDiscovery.Entry> misses) throws Exception {
        Map<String, Long> durations = new ConcurrentHashMap<>();
        if (misses.isEmpty()) return durations;
        SmokeSuiteScheduler.Plan plan = SmokeSuiteScheduler.plan(root, catalog, misses);
        int width = SmokeSuiteScheduler.width(root, plan.pooled().size());
        if (width > 1) {
            System.out.println("  smoke suite pool: " + plan.pooled().size() + " independent, "
                    + plan.chained().size() + " chained, width " + width);
            runPooled(root, plan.pooled(), width, durations);
        } else {
            for (SmokeDiscovery.Entry smoke : plan.pooled())
                durations.put(smoke.id, SmokeExecution.run(root, smoke));
        }
        for (SmokeDiscovery.Entry smoke : plan.chained())
            durations.put(smoke.id, SmokeExecution.run(root, smoke));
        return durations;
    }

    private static void runPooled(Path root, List<SmokeDiscovery.Entry> pooled, int width,
            Map<String, Long> durations) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(width);
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (SmokeDiscovery.Entry smoke : pooled)
                futures.add(pool.submit(() -> SmokeExecution.run(root, smoke)));
            Exception failure = null;
            for (int index = 0; index < futures.size(); index++) {
                SmokeDiscovery.Entry smoke = pooled.get(index);
                try {
                    durations.put(smoke.id, futures.get(index).get());
                } catch (ExecutionException error) {
                    if (failure == null) failure = new IllegalStateException(
                            "pooled smoke failed: " + smoke.id, error.getCause());
                    pool.shutdownNow();
                } catch (CancellationException error) {
                    if (failure == null) failure = new IllegalStateException(
                            "pooled smoke was cancelled: " + smoke.id);
                }
            }
            if (failure != null) throw failure;
        } finally {
            pool.shutdownNow();
        }
    }
}
