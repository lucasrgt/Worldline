package worldline.smoke.b173piston;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.PistonSubsystemObservation;
import worldline.testapi.PistonSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for the complete piston conformance probe. */
final class PistonSubsystemBackend implements GameBackend, PistonSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    PistonSubsystemBackend(long seed) {
        this.seed = seed;
        this.trace = new CanonicalTrace(seed);
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new PistonMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }
    @Override public void close() {
        world = null;
    }

    @Override public PistonSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("piston world is not loaded");
        PistonDomainProbe domain = PistonDomainProbe.execute(world);
        PistonLifecycleProbe lifecycle = PistonLifecycleProbe.execute(world);
        PistonPhysicalProbe physical = PistonPhysicalProbe.execute(world);
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
