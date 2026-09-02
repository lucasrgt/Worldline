package worldline.smoke.b173builtenvironment;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.BuiltEnvironmentMaterialsObservation;
import worldline.testapi.BuiltEnvironmentMaterialsScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for the complete construction-material matrix. */
final class BuiltEnvironmentMaterialsBackend
        implements GameBackend, BuiltEnvironmentMaterialsScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    BuiltEnvironmentMaterialsBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new BuiltEnvironmentMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 5; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }

    @Override public void close() { world = null; }

    @Override public BuiltEnvironmentMaterialsObservation observe() {
        if (world == null)
            throw new IllegalStateException("built-environment world is not loaded");
        BuiltEnvironmentMaterialsProbe probe = BuiltEnvironmentMaterialsProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("material-" + probe.rows[index][0], index + 1, 0, probe.rows[index]);
        return new BuiltEnvironmentMaterialsObservation(probe.states(), probe.shapes(),
                probe.light(), probe.ticks(), probe.neighbors());
    }

    void emit() { trace.emitTo(System.out); }
}
