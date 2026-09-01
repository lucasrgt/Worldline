package worldline.smoke.b173redstoneinputs;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testkit.RedstoneInputControlsSubsystemObservation;
import worldline.testkit.RedstoneInputControlsSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for the complete redstone input-control package. */
final class RedstoneInputControlsBackend
        implements GameBackend, RedstoneInputControlsSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    RedstoneInputControlsBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new RedstoneInputsMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -3; chunkX <= 3; chunkX++)
            for (int chunkZ = -3; chunkZ <= 3; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }
    @Override public void close() { world = null; }

    @Override public RedstoneInputControlsSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("redstone input-control world is not loaded");
        RedstoneInputControlsProbe probe = RedstoneInputControlsProbe.execute(world);
        trace.record("lever", 1, 0, probe.lever);
        trace.record("button", 2, 0, probe.button);
        trace.record("stone-plate", 3, 0, probe.stonePlate);
        trace.record("wooden-plate", 4, 0, probe.woodenPlate);
        trace.record("support", 5, 4, probe.supportMask);
        return new RedstoneInputControlsSubsystemObservation(probe.lever(), probe.button(),
                probe.stonePlate(), probe.woodenPlate(), probe.support());
    }

    void emit() { trace.emitTo(System.out); }
}
