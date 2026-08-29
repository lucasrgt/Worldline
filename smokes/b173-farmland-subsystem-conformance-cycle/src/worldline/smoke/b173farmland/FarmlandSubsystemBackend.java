package worldline.smoke.b173farmland;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testkit.FarmlandSubsystemObservation;
import worldline.testkit.FarmlandSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete farmland conformance. */
final class FarmlandSubsystemBackend implements GameBackend, FarmlandSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    FarmlandSubsystemBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }
    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new FarmlandMemorySaveHandler(seed, name), name, seed, null);
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
    @Override public FarmlandSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("farmland world is not loaded");
        FarmlandSubsystemProbe probe = FarmlandSubsystemProbe.execute(world);
        trace.record("placement", 1, 0, probe.placementRoute, probe.placedState,
                probe.stackAfter, probe.metadataMask);
        trace.record("lifecycle", 2, probe.dropDelta, probe.strengthClass,
                probe.breakBefore, probe.breakAfter, probe.dropId, probe.dropCount);
        trace.record("persistence", 3, 0, probe.savedState);
        trace.record("physics", 4, 0, probe.collisionFull, probe.visualHeight, probe.opaque,
                probe.cube, probe.lightCode, probe.tickMask, probe.hydratedState,
                probe.dryState, probe.stableState, probe.coverBefore, probe.coverAfter);
        return new FarmlandSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.persistence(), probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
