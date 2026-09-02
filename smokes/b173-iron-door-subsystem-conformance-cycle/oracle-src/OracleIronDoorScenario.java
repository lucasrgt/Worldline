import worldline.testapi.IronDoorSubsystemObservation;
import worldline.testapi.IronDoorSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped iron-door scenario. */
final class OracleIronDoorScenario implements IronDoorSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleIronDoorScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }
    @Override public IronDoorSubsystemObservation observe() {
        OracleIronDoorProbe probe = OracleIronDoorProbe.execute(world);
        trace.record("domain", 1, 0, probe.lowerMask, probe.upperMask);
        trace.record("lifecycle", 2, probe.lowerDropCount + probe.upperDropDelta,
                probe.strengthClass, probe.lowerBefore, probe.lowerAfter,
                probe.lowerDropId, probe.lowerDropCount, probe.upperBefore,
                probe.upperAfter, probe.upperDropDelta);
        trace.record("physics", 3, 0, probe.closedCollision, probe.openCollision,
                probe.opaque, probe.cube, probe.lightCode, probe.tickMask,
                probe.tickLowerBefore, probe.tickLowerAfter, probe.tickUpperBefore,
                probe.tickUpperAfter);
        trace.record("neighbors", 4, probe.orphanLowerDropCount
                + probe.orphanUpperDropDelta + probe.supportDropCount,
                probe.pairLower, probe.pairUpper, probe.orphanLower,
                probe.orphanLowerDropId, probe.orphanLowerDropCount,
                probe.orphanUpper, probe.orphanUpperDropDelta, probe.supportLower,
                probe.supportUpper, probe.supportDropId, probe.supportDropCount);
        return new IronDoorSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
