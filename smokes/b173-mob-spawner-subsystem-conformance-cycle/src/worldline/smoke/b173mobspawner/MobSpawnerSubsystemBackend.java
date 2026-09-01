package worldline.smoke.b173mobspawner;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testkit.MobSpawnerSubsystemObservation;
import worldline.testkit.MobSpawnerSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete mob-spawner conformance. */
final class MobSpawnerSubsystemBackend implements GameBackend, MobSpawnerSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    MobSpawnerSubsystemBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }
    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new MobSpawnerMemorySaveHandler(seed, name), name, seed, null);
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
    @Override public MobSpawnerSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("mob-spawner world is not loaded");
        MobSpawnerSubsystemProbe probe = MobSpawnerSubsystemProbe.execute(world);
        trace.record("registry", 1, 0, probe.registryMask);
        trace.record("placement", 2, 0, probe.placementRoute, probe.placedState,
                probe.stackAfter, probe.placedTile);
        trace.record("lifecycle", 3, probe.dropDelta, probe.strengthClass,
                probe.breakBefore, probe.breakAfter);
        trace.record("persistence", 4, 0, probe.savedState, probe.savedEntity,
                probe.savedDelay);
        trace.record("timing", 5, 1, probe.tickMask, probe.farDelay, probe.nearDelay);
        trace.record("neighbors", 6, 0, probe.neighborState, probe.neighborEntity,
                probe.neighborDelay);
        return new MobSpawnerSubsystemObservation(probe.registry(), probe.placement(),
                probe.lifecycle(), probe.persistence(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
