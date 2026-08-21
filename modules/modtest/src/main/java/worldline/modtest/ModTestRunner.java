package worldline.modtest;

import java.nio.file.Path;

/**
 * Neutral contract for one attested controlled-runtime mod execution. Adapters
 * own the implementation; the CLI binds one reflectively by provider name.
 */
public interface ModTestRunner {
    /**
     * Inspects, loads, and executes {@code modJar} for exactly {@code ticks}
     * controlled ticks under {@code seed}, returning an executed result whose
     * trace was produced by that run.
     */
    ModTestResult run(Path modJar, long seed, int ticks);
}
