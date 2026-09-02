import worldline.testapi.BedSubsystemObservation;
import worldline.testapi.BedSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped bed scenario. */
final class OracleBedScenario implements BedSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleBedScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }
    @Override public BedSubsystemObservation observe() {
        OracleBedProbe probe = OracleBedProbe.execute(world);
        trace.record("domain", 1, 0, probe.footMask, probe.headMask);
        trace.record("lifecycle", 2, probe.footDropCount + probe.headDropDelta,
                probe.strengthClass, probe.footBefore, probe.footAfter,
                probe.footDropId, probe.footDropCount, probe.headBefore,
                probe.headAfter, probe.headDropDelta);
        trace.record("physics", 3, 0, probe.collision, probe.height, probe.opaque,
                probe.cube, probe.lightCode, probe.tickMask, probe.tickFootBefore,
                probe.tickFootAfter, probe.tickHeadBefore, probe.tickHeadAfter);
        trace.record("neighbors", 4, probe.orphanFootDropCount + probe.orphanHeadDropDelta,
                probe.pairFoot, probe.pairHead, probe.orphanFoot, probe.orphanFootDropId,
                probe.orphanFootDropCount, probe.orphanHead, probe.orphanHeadDropDelta);
        return new BedSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
