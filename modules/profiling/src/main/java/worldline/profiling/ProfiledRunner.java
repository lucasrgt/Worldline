package worldline.profiling;

import worldline.minimization.Scenario;
import worldline.trace.CanonicalStateDocument;

/**
 * Neutral contract for one profiled scenario execution. Implementations run
 * the controlled runtime and return per-tick wall-clock samples; adapters own
 * them and the CLI binds one reflectively by provider name.
 */
public interface ProfiledRunner {
    /**
     * Executes every step of {@code scenario} under {@code seed}. Only
     * controlled ticks are sampled; other steps are setup and unmeasured.
     * The returned trace documents the behavioral side of the same run.
     */
    TickProfiledRun profile(Scenario scenario, long seed);
}
