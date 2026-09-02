package worldline.smoke.b173openable;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.OpenableAccessStructuresObservation;
import worldline.testapi.OpenableAccessStructuresScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for the openable access structures matrix. */
final class OpenableAccessStructuresBackend implements GameBackend, OpenableAccessStructuresScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    OpenableAccessStructuresBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new OpenableAccessMemorySaveHandler(seed, name), name, seed, null);
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

    @Override public OpenableAccessStructuresObservation observe() {
        if (world == null)
            throw new IllegalStateException("openable access structures world is absent");
        OpenableAccessStructuresProbe probe = OpenableAccessStructuresProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("openable-access-" + probe.rows[index][0], index + 1, 0,
                    probe.rows[index]);
        return probe.observation();
    }

    void emit() {
        trace.emitTo(System.out);
    }
}
