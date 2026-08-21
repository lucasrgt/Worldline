package worldline.minimization;

import worldline.trace.CanonicalStateDocument;

/**
 * Neutral contract for executing a public-grammar scenario against one
 * controlled runtime. Adapters own the implementation; callers bind one
 * reflectively by provider name.
 */
public interface ScenarioRunner {
    /**
     * Boots the controlled runtime under {@code seed}, applies every scenario
     * step in order, and returns the canonical trace of the run.
     */
    CanonicalStateDocument run(Scenario scenario, long seed);
}
