package worldline.smoke.b173redstonetorch;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testkit.RedstoneTorchSubsystemObservation;
import worldline.testkit.RedstoneTorchSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete redstone torch conformance. */
final class RedstoneTorchSubsystemBackend implements GameBackend, RedstoneTorchSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    RedstoneTorchSubsystemBackend(long seed) {
        this.seed = seed;
        this.trace = new CanonicalTrace(seed);
    }
    @Override
    public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new RedstoneTorchMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }
    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }
    @Override
    public void close() {
        world = null;
    }
    @Override public RedstoneTorchSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("redstone torch world is not loaded");
        RedstoneTorchTimingProbe timing = RedstoneTorchTimingProbe.execute(world);
        RedstoneTorchLifecycleProbe lifecycle = RedstoneTorchLifecycleProbe.execute(world);
        trace.record("domains", 1, 0, timing.offMask, timing.onMask, timing.faceMask);
        trace.record("timing", 2, 0, timing.delay, timing.burnoutCount,
                timing.burnoutHold, timing.recoveryAge, timing.recoveryDelay);
        trace.record("lifecycle", 3, lifecycle.dropCount, lifecycle.breakAfter,
                lifecycle.dropStack, lifecycle.savedOff, lifecycle.savedOn);
        trace.record("physics", 4, 0, lifecycle.offCollision, lifecycle.onCollision,
                lifecycle.lightCode, timing.randomMask, timing.supportAfter, timing.supportDrop);
        return new RedstoneTorchSubsystemObservation(timing.domains(), timing.materialization(),
                lifecycle.lifecycle(), lifecycle.physics(), timing.timing(), timing.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
