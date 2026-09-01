package worldline.smoke.b173fire;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testkit.FireSubsystemObservation;
import worldline.testkit.FireSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete fire conformance. */
final class FireSubsystemBackend implements GameBackend, FireSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    FireSubsystemBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }
    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new FireMemorySaveHandler(seed, name), name, seed, null);
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
    @Override public FireSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("fire world is not loaded");
        FireSubsystemProbe probe = FireSubsystemProbe.execute(world);
        trace.record("placement", 1, 0, probe.placementRoute, probe.placedState,
                probe.stackAfter, probe.metadataMask);
        trace.record("lifecycle", 2, probe.dropDelta, probe.strengthClass,
                probe.breakBefore, probe.breakAfter);
        trace.record("persistence", 3, 0, probe.savedState);
        trace.record("physics", 4, 0, probe.collisionNull, probe.collidable, probe.lightCode,
                probe.tickMask, probe.tickRate, probe.supportedState,
                probe.lossBefore, probe.lossAfter);
        return new FireSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.persistence(), probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
