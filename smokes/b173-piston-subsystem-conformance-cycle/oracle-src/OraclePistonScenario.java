import worldline.testapi.PistonSubsystemObservation;
import worldline.testapi.PistonSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped piston scenario. */
final class OraclePistonScenario implements PistonSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OraclePistonScenario(dj world, long seed) {
        this.world = world;
        this.trace = new CanonicalTrace(seed);
    }

    @Override public PistonSubsystemObservation observe() {
        OraclePistonDomain domain = OraclePistonDomain.execute(world);
        OraclePistonLifecycle lifecycle = OraclePistonLifecycle.execute(world);
        OraclePistonPhysical physical = OraclePistonPhysical.execute(world);
        trace.record("domains", 1, 0, domain.baseMask, domain.headMask, domain.movingMask);
        trace.record("materialization", 2, 0, domain.normalMoving, domain.normalHead,
                domain.stickyMoving, domain.stickyHead);
        trace.record("break_drop", 3, lifecycle.dropCount, lifecycle.headAfter,
                lifecycle.baseAfter, lifecycle.headDrop, lifecycle.movingAfter, lifecycle.movingDrop);
        trace.record("persistence", 4, 0, lifecycle.savedHead, lifecycle.savedMoving,
                lifecycle.storedId, lifecycle.storedMetadata, lifecycle.storedDirection,
                lifecycle.extending ? 1 : 0);
        trace.record("physics", 5, 0, physical.baseBoxes, physical.headBoxes,
                physical.movingBoxes, physical.lightSum, physical.randomMask, physical.idleTicks);
        trace.record("transitions", 6, 0, physical.normalExtended, physical.normalRetracted,
                physical.stickyExtended, physical.stickyRetracted, physical.headUnsupported,
                physical.movingHeld, physical.movingSettled);
        return new PistonSubsystemObservation(domain.domains(), domain.materialization(),
                lifecycle.breakAndDrops(), lifecycle.persistence(), physical.collision(),
                physical.light(), physical.ticks(), physical.neighbors());
    }

    void emit() {
        trace.emitTo(System.out);
    }
}
