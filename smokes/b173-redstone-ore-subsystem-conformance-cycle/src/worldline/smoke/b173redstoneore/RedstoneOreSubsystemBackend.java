package worldline.smoke.b173redstoneore;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.RedstoneOreSubsystemObservation;
import worldline.testapi.RedstoneOreSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete redstone-ore conformance. */
final class RedstoneOreSubsystemBackend implements GameBackend, RedstoneOreSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    RedstoneOreSubsystemBackend(long seed) {
        this.seed = seed;
        this.trace = new CanonicalTrace(seed);
    }
    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new RedstoneOreMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }
    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }
    @Override public void close() { world = null; }
    @Override public RedstoneOreSubsystemObservation observe() {
        if (world == null) throw new IllegalStateException("redstone ore world is not loaded");
        RedstoneOreDomainProbe domain = RedstoneOreDomainProbe.execute(world);
        RedstoneOreLifecycleProbe lifecycle = RedstoneOreLifecycleProbe.execute(world);
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
