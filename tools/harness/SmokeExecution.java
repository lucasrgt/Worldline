import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Observes one canonical smoke execution without changing its behavioral fingerprint. */
final class SmokeExecution {
    private SmokeExecution() { }

    static long run(Path root, SmokeDiscovery.Entry smoke) throws Exception {
        return run(root, smoke, null);
    }

    static long run(Path root, SmokeDiscovery.Entry smoke, Path productRoot) throws Exception {
        long started = System.nanoTime();
        SmokeProcess process = new SmokeProcess(root, productRoot);
        try {
            long duration = process.run(smoke);
            new SmokeScheduleHistory(root).observed(smoke.id, true, duration, process.telemetry());
            return duration;
        } catch (Exception error) {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            new SmokeScheduleHistory(root).observed(smoke.id, false, elapsed, process.telemetry());
            throw error;
        }
    }
}
