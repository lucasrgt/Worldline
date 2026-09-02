package worldline.smoke.b173redstonesignals;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.RedstoneSignalConsumersObservation;
import worldline.testapi.RedstoneSignalConsumersScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for the complete redstone signal-consumer matrix. */
final class RedstoneSignalConsumersBackend implements GameBackend, RedstoneSignalConsumersScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    RedstoneSignalConsumersBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }

    @Override
    public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new RedstoneSignalMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override
    public void tick() {
        world.updateEntities();
        world.tick();
    }

    @Override
    public void close() {
        world = null;
    }

    @Override
    public RedstoneSignalConsumersObservation observe() {
        if (world == null)
            throw new IllegalStateException("redstone signal-consumer world is not loaded");
        RedstoneSignalConsumersProbe probe = RedstoneSignalConsumersProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("redstone-consumer-" + probe.rows[index][0], index + 1, 0,
                    probe.rows[index]);
        return new RedstoneSignalConsumersObservation(probe.states(), probe.shapes(),
                probe.light(), probe.ticks(), probe.neighbors());
    }

    void emit() {
        trace.emitTo(System.out);
    }
}
