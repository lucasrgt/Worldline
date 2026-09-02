package worldline.smoke.b173furnace;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.FurnaceSubsystemObservation;
import worldline.testapi.FurnaceSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete furnace conformance. */
final class FurnaceSubsystemBackend implements GameBackend, FurnaceSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    FurnaceSubsystemBackend(long seed) {
        this.seed = seed;
        this.trace = new CanonicalTrace(seed);
    }
    @Override
    public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override
    public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new FurnaceMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }
    @Override
    public void tick() {
        world.updateEntities();
        world.tick();
    }
    @Override
    public void close() {
        world = null;
    }
    @Override
    public FurnaceSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("furnace world is not loaded");
        FurnaceDomainProbe domain = FurnaceDomainProbe.execute(world);
        FurnaceLifecycleProbe lifecycle = FurnaceLifecycleProbe.execute(world);
        trace.record("domains", 1, 0, domain.idleMask, domain.activeMask,
                domain.ignition, domain.progress);
        trace.record("timing", 2, 0, domain.completion, domain.extinction,
                domain.neighborCode, domain.tickMask);
        trace.record("lifecycle", 3, lifecycle.dropCount, lifecycle.breakAfter,
                lifecycle.dropCode, lifecycle.savedState, lifecycle.savedProgress);
        trace.record("physics", 4, 0, (int) (lifecycle.savedSlots / 100000000L),
                (int) (lifecycle.savedSlots % 100000000L), lifecycle.idleCollision,
                lifecycle.activeCollision, lifecycle.lightCode);
        return new FurnaceSubsystemObservation(domain.domains(), domain.materialization(),
                lifecycle.lifecycle(), lifecycle.physics(), domain.timing(), domain.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
