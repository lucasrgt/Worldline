import worldline.testkit.FarmlandSubsystemObservation;
import worldline.testkit.FarmlandSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped farmland scenario. */
final class OracleFarmlandScenario implements FarmlandSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleFarmlandScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }
    @Override public FarmlandSubsystemObservation observe() {
        OracleFarmlandProbe probe = OracleFarmlandProbe.execute(world);
        trace.record("placement", 1, 0, probe.placementRoute, probe.placedState,
                probe.stackAfter, probe.metadataMask);
        trace.record("lifecycle", 2, probe.dropDelta, probe.strengthClass,
                probe.breakBefore, probe.breakAfter, probe.dropId, probe.dropCount);
        trace.record("persistence", 3, 0, probe.savedState);
        trace.record("physics", 4, 0, probe.collisionFull, probe.visualHeight, probe.opaque,
                probe.cube, probe.lightCode, probe.tickMask, probe.hydratedState,
                probe.dryState, probe.stableState, probe.coverBefore, probe.coverAfter);
        return new FarmlandSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.persistence(), probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
