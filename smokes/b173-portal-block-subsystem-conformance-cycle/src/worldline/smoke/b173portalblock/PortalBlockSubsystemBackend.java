package worldline.smoke.b173portalblock;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.PortalBlockSubsystemObservation;
import worldline.testapi.PortalBlockSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete portal-block conformance. */
final class PortalBlockSubsystemBackend implements GameBackend, PortalBlockSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    PortalBlockSubsystemBackend(long seed) {
        this.seed = seed;
        this.trace = new CanonicalTrace(seed);
    }
    @Override public void bootHeadless() { System.setProperty("java.awt.headless", "true"); }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new PortalBlockMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }
    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }
    @Override public void close() { world = null; }
    @Override public PortalBlockSubsystemObservation observe() {
        if (world == null) throw new IllegalStateException("portal block world is not loaded");
        PortalBlockDomainProbe domain = PortalBlockDomainProbe.execute(world);
        PortalBlockLifecycleProbe lifecycle = PortalBlockLifecycleProbe.execute(world);
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
