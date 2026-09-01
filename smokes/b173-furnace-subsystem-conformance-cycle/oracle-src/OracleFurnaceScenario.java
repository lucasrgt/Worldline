import worldline.testkit.FurnaceSubsystemObservation;
import worldline.testkit.FurnaceSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped furnace scenario. */
final class OracleFurnaceScenario implements FurnaceSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleFurnaceScenario(dj world, long seed) {
        this.world = world;
        this.trace = new CanonicalTrace(seed);
    }
    @Override
    public FurnaceSubsystemObservation observe() {
        OracleFurnaceDomain domain = OracleFurnaceDomain.execute(world);
        OracleFurnaceLifecycle lifecycle = OracleFurnaceLifecycle.execute(world);
        trace.record("domains", 1, 0, domain.idleMask, domain.activeMask,
                domain.ignition, domain.progress);
        trace.record("timing", 2, 0, domain.completion, domain.extinction,
                domain.neighborCode, domain.tickMask);
        trace.record("lifecycle", 3, lifecycle.dropCount, lifecycle.breakAfter,
                lifecycle.dropCode, lifecycle.savedState, lifecycle.savedProgress);
        trace.record("physics", 4, 0, (int) (lifecycle.savedSlots / 100000000L),
                (int) (lifecycle.savedSlots % 100000000L), lifecycle.idleCollision,
                lifecycle.activeCollision, lifecycle.lightCode);
        return new FurnaceSubsystemObservation(domain.domains(), domain.materialization(),
                lifecycle.lifecycle(), lifecycle.physics(), domain.timing(), domain.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
