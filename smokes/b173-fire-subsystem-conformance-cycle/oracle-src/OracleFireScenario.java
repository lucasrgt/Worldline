import worldline.testkit.FireSubsystemObservation;
import worldline.testkit.FireSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped fire scenario. */
final class OracleFireScenario implements FireSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleFireScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }
    @Override public FireSubsystemObservation observe() {
        OracleFireProbe probe = OracleFireProbe.execute(world);
        trace.record("placement", 1, 0, probe.placementRoute, probe.placedState,
                probe.stackAfter, probe.metadataMask);
        trace.record("lifecycle", 2, probe.dropDelta, probe.strengthClass,
                probe.breakBefore, probe.breakAfter);
        trace.record("persistence", 3, 0, probe.savedState);
        trace.record("physics", 4, 0, probe.collisionNull, probe.collidable, probe.lightCode,
                probe.tickMask, probe.tickRate, probe.supportedState,
                probe.lossBefore, probe.lossAfter);
        return new FireSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.persistence(), probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
