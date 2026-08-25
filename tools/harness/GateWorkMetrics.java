import java.util.concurrent.atomic.AtomicInteger;

/** Counts cache misses that make an otherwise restored Gate run cold. */
final class GateWorkMetrics {
    private static final AtomicInteger MODULES = new AtomicInteger();
    private static final AtomicInteger TEST_MODULES = new AtomicInteger();
    private static final AtomicInteger TEST_SUITES = new AtomicInteger();
    private static final AtomicInteger SMOKE_RUNNERS = new AtomicInteger();

    private GateWorkMetrics() { }

    static void reset() {
        MODULES.set(0); TEST_MODULES.set(0); TEST_SUITES.set(0); SMOKE_RUNNERS.set(0);
    }

    static void moduleCompiled() { MODULES.incrementAndGet(); }
    static void testModuleCompiled() { TEST_MODULES.incrementAndGet(); }
    static void testSuiteExecuted() { TEST_SUITES.incrementAndGet(); }
    static void smokeRunnersCompiled(int count) { SMOKE_RUNNERS.addAndGet(count); }

    static Metrics metrics() {
        return new Metrics(MODULES.get(), TEST_MODULES.get(), TEST_SUITES.get(), SMOKE_RUNNERS.get());
    }

    record Metrics(int modulesCompiled, int testModulesCompiled,
            int testSuitesExecuted, int smokeRunnersCompiled) {
        boolean fullyRestored() {
            return modulesCompiled == 0 && testModulesCompiled == 0
                    && testSuitesExecuted == 0 && smokeRunnersCompiled == 0;
        }
    }
}
