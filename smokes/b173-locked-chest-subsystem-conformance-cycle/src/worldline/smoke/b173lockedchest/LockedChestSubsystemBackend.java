package worldline.smoke.b173lockedchest;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.LockedChestSubsystemObservation;
import worldline.testapi.LockedChestSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete locked-chest conformance. */
final class LockedChestSubsystemBackend implements GameBackend, LockedChestSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    LockedChestSubsystemBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }
    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new LockedChestMemorySaveHandler(seed, name), name, seed, null);
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
    @Override public LockedChestSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("locked-chest world is not loaded");
        LockedChestSubsystemProbe probe = LockedChestSubsystemProbe.execute(world);
        trace.record("placement", 1, 0, probe.placementRoute, probe.placedState,
                probe.stackAfter, probe.metadataMask);
        trace.record("lifecycle", 2, probe.dropDelta, probe.strengthClass,
                probe.breakBefore, probe.breakAfter, probe.dropId, probe.dropCount);
        trace.record("persistence", 3, 0, probe.savedState);
        trace.record("physics", 4, 0, probe.collision, probe.lightCode,
                probe.tickMask, probe.tickBefore, probe.tickAfter, probe.neighborState);
        return new LockedChestSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.persistence(), probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
