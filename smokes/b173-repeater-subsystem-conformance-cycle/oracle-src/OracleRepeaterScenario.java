import worldline.testkit.RepeaterSubsystemObservation;
import worldline.testkit.RepeaterSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped repeater scenario. */
final class OracleRepeaterScenario implements RepeaterSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OracleRepeaterScenario(dj world, long seed) {
        this.world = world;
        this.trace = new CanonicalTrace(seed);
    }

    @Override public RepeaterSubsystemObservation observe() {
        OracleRepeaterTiming timing = OracleRepeaterTiming.execute(world);
        OracleRepeaterLifecycle lifecycle = OracleRepeaterLifecycle.execute(world);
        trace.record("domains", 1, 0, timing.offMask, timing.onMask);
        trace.record("timing", 2, 0, timing.powerTicks, timing.releaseTicks,
                timing.stableTicks, timing.directionMask);
        trace.record("lifecycle", 3, lifecycle.dropCount, lifecycle.breakAfter,
                lifecycle.dropStack, lifecycle.savedOff, lifecycle.savedOn);
        trace.record("physics", 4, 0, lifecycle.offBoxes, lifecycle.onBoxes,
                lifecycle.offHeight, lifecycle.onHeight, lifecycle.lightCode,
                timing.randomMask, timing.supportAfter, timing.supportDrop);
        return new RepeaterSubsystemObservation(timing.domains(), timing.materialization(),
                lifecycle.lifecycle(), lifecycle.physics(), timing.timing(), timing.neighbors());
    }

    void emit() { trace.emitTo(System.out); }
}
