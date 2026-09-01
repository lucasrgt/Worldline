import worldline.testkit.MobSpawnerSubsystemObservation;
import worldline.testkit.MobSpawnerSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped mob-spawner scenario. */
final class OracleMobSpawnerScenario implements MobSpawnerSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleMobSpawnerScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }
    @Override public MobSpawnerSubsystemObservation observe() {
        OracleMobSpawnerProbe probe = OracleMobSpawnerProbe.execute(world);
        trace.record("registry", 1, 0, probe.registryMask);
        trace.record("placement", 2, 0, probe.placementRoute, probe.placedState,
                probe.stackAfter, probe.placedTile);
        trace.record("lifecycle", 3, probe.dropDelta, probe.strengthClass,
                probe.breakBefore, probe.breakAfter);
        trace.record("persistence", 4, 0, probe.savedState, probe.savedEntity,
                probe.savedDelay);
        trace.record("timing", 5, 1, probe.tickMask, probe.farDelay, probe.nearDelay);
        trace.record("neighbors", 6, 0, probe.neighborState, probe.neighborEntity,
                probe.neighborDelay);
        return new MobSpawnerSubsystemObservation(probe.registry(), probe.placement(),
                probe.lifecycle(), probe.persistence(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
