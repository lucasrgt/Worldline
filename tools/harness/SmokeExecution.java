import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Observes one canonical smoke execution without changing its behavioral fingerprint. */
final class SmokeExecution {
    private SmokeExecution() { }

    static long run(Path root, SmokeDiscovery.Entry smoke) throws Exception {
        long started = System.nanoTime();
        try {
            long duration = new SmokeProcess(root).run(smoke);
            new SmokeScheduleHistory(root).observed(smoke.id, true, duration);
            return duration;
        } catch (Exception error) {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            new SmokeScheduleHistory(root).observed(smoke.id, false, elapsed);
            throw error;
        }
    }
}
