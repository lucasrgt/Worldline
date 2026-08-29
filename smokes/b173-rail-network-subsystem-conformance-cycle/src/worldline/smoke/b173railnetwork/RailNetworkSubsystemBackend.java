package worldline.smoke.b173railnetwork;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testkit.RailNetworkSubsystemObservation;
import worldline.testkit.RailNetworkSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for the complete rail-network package. */
final class RailNetworkSubsystemBackend implements GameBackend, RailNetworkSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;

    RailNetworkSubsystemBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new RailNetworkMemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override public void tick() {
        world.updateEntities();
        world.tick();
    }

    @Override public void close() { world = null; }

    @Override public RailNetworkSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("rail-network world is not loaded");
        RailNetworkSubsystemProbe probe = RailNetworkSubsystemProbe.execute(world);
        trace.record("normal", 1, 0, probe.normalRail);
        trace.record("powered", 2, 0, probe.poweredRail);
        trace.record("detector", 3, 1, probe.detectorRail);
        trace.record("support", 4, 3, probe.supportMask);
        return new RailNetworkSubsystemObservation(probe.normalRail(), probe.poweredRail(),
                probe.detectorRail(), probe.support());
    }

    void emit() { trace.emitTo(System.out); }
}
