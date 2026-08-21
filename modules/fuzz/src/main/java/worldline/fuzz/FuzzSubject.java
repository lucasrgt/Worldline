package worldline.fuzz;

import worldline.minimization.Scenario;
import worldline.trace.CanonicalStateDocument;

/**
 * One fuzzing subject: a named executor of public-grammar scenarios.
 * Implementations are adapter-owned and bound reflectively by the CLI.
 */
public interface FuzzSubject {
    /** Stable, printable identity used in reports; lowercase bounded text. */
    String label();

    /** Executes the scenario under the controlled runtime and returns the trace. */
    CanonicalStateDocument run(Scenario scenario, long seed);
}
