package worldline.smoke.b173fluidfrozen;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.FluidFrozenMatterObservation;
import worldline.testapi.FluidFrozenMatterScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for the fluid and frozen-matter matrix. */
final class FluidFrozenMatterBackend implements GameBackend, FluidFrozenMatterScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    FluidFrozenMatterBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new FluidFrozenMemorySaveHandler(seed, name), name, seed, null);
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

    @Override public FluidFrozenMatterObservation observe() {
        if (world == null)
            throw new IllegalStateException("fluid and frozen-matter world is absent");
        FluidFrozenMatterProbe probe = FluidFrozenMatterProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("fluid-frozen-" + probe.rows[index][0], index + 1, 0,
                    probe.rows[index]);
        return probe.observation();
    }

    void emit() {
        trace.emitTo(System.out);
    }
}
