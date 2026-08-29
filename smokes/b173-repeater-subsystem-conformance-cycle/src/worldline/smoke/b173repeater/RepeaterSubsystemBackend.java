package worldline.smoke.b173repeater;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testkit.RepeaterSubsystemObservation;
import worldline.testkit.RepeaterSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete repeater conformance. */
final class RepeaterSubsystemBackend implements GameBackend, RepeaterSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    RepeaterSubsystemBackend(long seed) {
        this.seed = seed;
        this.trace = new CanonicalTrace(seed);
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new RepeaterMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }
    @Override public void close() { world = null; }

    @Override public RepeaterSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("repeater world is not loaded");
        RepeaterTimingProbe timing = RepeaterTimingProbe.execute(world);
        RepeaterLifecycleProbe lifecycle = RepeaterLifecycleProbe.execute(world);
        trace.record("domains", 1, 0, timing.offMask, timing.onMask);
        trace.record("timing", 2, 0, timing.powerTicks, timing.releaseTicks,
                timing.stableTicks, timing.directionMask);
        trace.record("lifecycle", 3, lifecycle.dropCount, lifecycle.breakAfter,
                lifecycle.dropStack, lifecycle.savedOff, lifecycle.savedOn);
        trace.record("physics", 4, 0, lifecycle.offBoxes, lifecycle.onBoxes,
                lifecycle.offHeight, lifecycle.onHeight, lifecycle.lightCode,
                timing.randomMask, timing.supportAfter, timing.supportDrop);
        return new RepeaterSubsystemObservation(timing.domains(), timing.materialization(),
                lifecycle.lifecycle(), lifecycle.physics(), timing.timing(), timing.neighbors());
    }

    void emit() { trace.emitTo(System.out); }
}
