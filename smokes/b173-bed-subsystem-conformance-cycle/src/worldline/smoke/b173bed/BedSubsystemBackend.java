package worldline.smoke.b173bed;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testkit.BedSubsystemObservation;
import worldline.testkit.BedSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete bed conformance. */
final class BedSubsystemBackend implements GameBackend, BedSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    BedSubsystemBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }
    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new BedMemorySaveHandler(seed, name), name, seed, null);
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
    @Override public BedSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("bed world is not loaded");
        BedSubsystemProbe probe = BedSubsystemProbe.execute(world);
        trace.record("domain", 1, 0, probe.footMask, probe.headMask);
        trace.record("lifecycle", 2, probe.footDropCount + probe.headDropDelta,
                probe.strengthClass, probe.footBefore, probe.footAfter,
                probe.footDropId, probe.footDropCount, probe.headBefore,
                probe.headAfter, probe.headDropDelta);
        trace.record("physics", 3, 0, probe.collision, probe.height, probe.opaque,
                probe.cube, probe.lightCode, probe.tickMask, probe.tickFootBefore,
                probe.tickFootAfter, probe.tickHeadBefore, probe.tickHeadAfter);
        trace.record("neighbors", 4, probe.orphanFootDropCount + probe.orphanHeadDropDelta,
                probe.pairFoot, probe.pairHead, probe.orphanFoot, probe.orphanFootDropId,
                probe.orphanFootDropCount, probe.orphanHead, probe.orphanHeadDropDelta);
        return new BedSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
