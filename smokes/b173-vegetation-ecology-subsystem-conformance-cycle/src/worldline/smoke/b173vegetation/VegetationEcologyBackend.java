package worldline.smoke.b173vegetation;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.VegetationEcologyObservation;
import worldline.testapi.VegetationEcologyScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for the complete vegetation ecology matrix. */
final class VegetationEcologyBackend implements GameBackend, VegetationEcologyScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    VegetationEcologyBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new VegetationMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }

    @Override public void close() { world = null; }

    @Override public VegetationEcologyObservation observe() {
        if (world == null)
            throw new IllegalStateException("vegetation world is not loaded");
        VegetationEcologyProbe probe = VegetationEcologyProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("vegetation-" + probe.rows[index][0], index + 1, 0,
                    probe.rows[index]);
        return new VegetationEcologyObservation(probe.states(), probe.shapes(),
                probe.light(), probe.neighbors());
    }

    void emit() { trace.emitTo(System.out); }
}
