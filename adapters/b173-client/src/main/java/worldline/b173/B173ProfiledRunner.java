package worldline.b173;

import java.nio.file.Paths;
import java.util.List;
import worldline.api.WorldSource;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;
import worldline.profiling.ProfiledRunner;
import worldline.profiling.TickProfile;
import worldline.profiling.TickProfiledRun;
import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateTrace;

/** Profiles per-tick wall-clock time of public-grammar scenario executions. */
public final class B173ProfiledRunner implements ProfiledRunner {
    @Override
    public TickProfiledRun profile(Scenario scenario, long seed) {
        List<worldline.minimization.ScenarioStep> steps = ScenarioDsl.parseAll(scenario);
        int ticks = tickCount(steps);
        require(ticks > 0, "profiling requires at least one tick step");
        long[] tickNanos = new long[ticks];
        long[] modNanos = new long[ticks];
        B173Runtime runtime = B173Runtimes.create(seed);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "profile-run")));
            CanonicalStateTrace trace = B173ScenarioOps.trace(runtime, seed);
            int sample = 0;
            for (worldline.minimization.ScenarioStep step : steps) {
                if (step.kind() != worldline.minimization.ScenarioStep.Kind.TICK) {
                    B173ScenarioOps.apply(step, runtime, trace);
                    continue;
                }
                for (int repeat = 0; repeat < step.count(); repeat++) {
                    long start = System.nanoTime();
                    runtime.tick();
                    tickNanos[sample] = System.nanoTime() - start;
                    modNanos[sample] = runtime.lastModNanos();
                    sample++;
                }
            }
            TickProfile profile = TickProfile.of(tickNanos, modNanos);
            return new TickProfiledRun(profile,
                    CanonicalStateDocument.parse(trace.value()));
        } finally { runtime.close(); }
    }

    private static int tickCount(List<worldline.minimization.ScenarioStep> steps) {
        int total = 0;
        for (worldline.minimization.ScenarioStep step : steps) {
            if (step.kind() == worldline.minimization.ScenarioStep.Kind.TICK) {
                total += step.count();
            }
        }
        return total;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
