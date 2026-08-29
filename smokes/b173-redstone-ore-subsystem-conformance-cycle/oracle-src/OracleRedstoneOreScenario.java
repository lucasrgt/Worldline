import worldline.testkit.RedstoneOreSubsystemObservation;
import worldline.testkit.RedstoneOreSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped redstone-ore scenario. */
final class OracleRedstoneOreScenario implements RedstoneOreSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;
    OracleRedstoneOreScenario(dj world, long seed) {
        this.world = world;
        this.trace = new CanonicalTrace(seed);
    }
    @Override public RedstoneOreSubsystemObservation observe() {
        OracleRedstoneOreDomain domain = OracleRedstoneOreDomain.execute(world);
        OracleRedstoneOreLifecycle lifecycle = OracleRedstoneOreLifecycle.execute(world);
        trace.record("registry", 1, 0, domain.registryMask,
                domain.activationBefore, domain.activationAfter);
        trace.record("timing", 2, 0, domain.randomMask, domain.fadeBefore,
                domain.fadeAfter, domain.unlitNeighbors, domain.glowingNeighbors);
        trace.record("lifecycle", 3, lifecycle.dropCount, lifecycle.breakAfter,
                lifecycle.dropItem, lifecycle.savedUnlit, lifecycle.savedGlowing);
        trace.record("physics", 4, 0, lifecycle.unlitCollision,
                lifecycle.glowingCollision, lifecycle.lightCode);
        return new RedstoneOreSubsystemObservation(domain.registry(), domain.domains(),
                lifecycle.lifecycle(), lifecycle.physics(), domain.timing(), domain.neighbors());
    }
    void emit() { trace.emitTo(System.out); }
}
