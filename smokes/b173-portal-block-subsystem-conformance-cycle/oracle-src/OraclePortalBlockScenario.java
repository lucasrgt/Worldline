import worldline.testapi.PortalBlockSubsystemObservation;
import worldline.testapi.PortalBlockSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped portal-block scenario. */
final class OraclePortalBlockScenario implements PortalBlockSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OraclePortalBlockScenario(dj world, long seed) {
        this.world = world;
        this.trace = new CanonicalTrace(seed);
    }
    @Override public PortalBlockSubsystemObservation observe() {
        OraclePortalBlockDomain domain = OraclePortalBlockDomain.execute(world);
        OraclePortalBlockLifecycle lifecycle = OraclePortalBlockLifecycle.execute(world);
        trace.record("domains", 1, 0, domain.xCells, domain.zCells, domain.metadataMask);
        trace.record("timing", 2, domain.entityDelta,
                domain.randomMask, domain.tickBefore, domain.tickAfter);
        trace.record("lifecycle", 3, lifecycle.dropCount,
                lifecycle.breakAfter, lifecycle.dropCount);
        trace.record("persistence", 4, 0,
                lifecycle.savedCount, lifecycle.savedStateSum);
        trace.record("physics", 5, 0, lifecycle.collision, lifecycle.lightCode,
                domain.neighborBefore, domain.neighborAfter);
        return new PortalBlockSubsystemObservation(domain.domains(), lifecycle.lifecycle(),
                lifecycle.persistence(), lifecycle.physics(), domain.timing(), domain.neighbors());
    }
    void emit() { trace.emitTo(System.out); }
}
