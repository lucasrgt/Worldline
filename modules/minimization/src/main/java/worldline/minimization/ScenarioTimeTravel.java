package worldline.minimization;

import worldline.trace.CanonicalStateDocument;

/**
 * Neutral time-travel contract for public-grammar scenarios. Implementations
 * replay deterministically, so asking for any prefix always yields the exact
 * same trace as running the full scenario and truncating it.
 */
public interface ScenarioTimeTravel {
    /**
     * Boots the controlled runtime under {@code seed}, applies exactly the
     * first {@code steps} steps of {@code scenario}, and returns the trace of
     * that prefix. Zero steps yields an empty-record document.
     */
    CanonicalStateDocument prefix(Scenario scenario, long seed, int steps);
}
