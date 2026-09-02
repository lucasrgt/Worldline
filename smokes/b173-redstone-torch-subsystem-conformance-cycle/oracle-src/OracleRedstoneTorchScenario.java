import worldline.testapi.RedstoneTorchSubsystemObservation;
import worldline.testapi.RedstoneTorchSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped redstone torch scenario. */
final class OracleRedstoneTorchScenario implements RedstoneTorchSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleRedstoneTorchScenario(dj world, long seed) {
        this.world = world;
        this.trace = new CanonicalTrace(seed);
    }
    @Override public RedstoneTorchSubsystemObservation observe() {
        OracleRedstoneTorchTiming timing = OracleRedstoneTorchTiming.execute(world);
        OracleRedstoneTorchLifecycle lifecycle = OracleRedstoneTorchLifecycle.execute(world);
        trace.record("domains", 1, 0, timing.offMask, timing.onMask, timing.faceMask);
        trace.record("timing", 2, 0, timing.delay, timing.burnoutCount,
                timing.burnoutHold, timing.recoveryAge, timing.recoveryDelay);
        trace.record("lifecycle", 3, lifecycle.dropCount, lifecycle.breakAfter,
                lifecycle.dropStack, lifecycle.savedOff, lifecycle.savedOn);
        trace.record("physics", 4, 0, lifecycle.offCollision, lifecycle.onCollision,
                lifecycle.lightCode, timing.randomMask, timing.supportAfter, timing.supportDrop);
        return new RedstoneTorchSubsystemObservation(timing.domains(), timing.materialization(),
                lifecycle.lifecycle(), lifecycle.physics(), timing.timing(), timing.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
