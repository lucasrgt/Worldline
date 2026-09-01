import worldline.testkit.LockedChestSubsystemObservation;
import worldline.testkit.LockedChestSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped locked-chest scenario. */
final class OracleLockedChestScenario implements LockedChestSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleLockedChestScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }
    @Override public LockedChestSubsystemObservation observe() {
        OracleLockedChestProbe probe = OracleLockedChestProbe.execute(world);
        trace.record("placement", 1, 0, probe.placementRoute, probe.placedState,
                probe.stackAfter, probe.metadataMask);
        trace.record("lifecycle", 2, probe.dropDelta, probe.strengthClass,
                probe.breakBefore, probe.breakAfter, probe.dropId, probe.dropCount);
        trace.record("persistence", 3, 0, probe.savedState);
        trace.record("physics", 4, 0, probe.collision, probe.lightCode,
                probe.tickMask, probe.tickBefore, probe.tickAfter, probe.neighborState);
        return new LockedChestSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.persistence(), probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
