package worldline.smoke.b173irondoor;

import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.testapi.IronDoorSubsystemObservation;
import worldline.testapi.IronDoorSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Mapped official-world backend for complete iron-door conformance. */
final class IronDoorSubsystemBackend implements GameBackend, IronDoorSubsystemScenario {
    private final long seed;
    private final CanonicalTrace trace;
    private World world;
    IronDoorSubsystemBackend(long seed) {
        this.seed = seed;
        trace = new CanonicalTrace(seed);
    }
    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new IronDoorMemorySaveHandler(seed, name), name, seed, null);
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
    @Override public IronDoorSubsystemObservation observe() {
        if (world == null)
            throw new IllegalStateException("iron-door world is not loaded");
        IronDoorSubsystemProbe probe = IronDoorSubsystemProbe.execute(world);
        trace.record("domain", 1, 0, probe.lowerMask, probe.upperMask);
        trace.record("lifecycle", 2, probe.lowerDropCount + probe.upperDropDelta,
                probe.strengthClass, probe.lowerBefore, probe.lowerAfter,
                probe.lowerDropId, probe.lowerDropCount, probe.upperBefore,
                probe.upperAfter, probe.upperDropDelta);
        trace.record("physics", 3, 0, probe.closedCollision, probe.openCollision,
                probe.opaque, probe.cube, probe.lightCode, probe.tickMask,
                probe.tickLowerBefore, probe.tickLowerAfter, probe.tickUpperBefore,
                probe.tickUpperAfter);
        trace.record("neighbors", 4, probe.orphanLowerDropCount
                + probe.orphanUpperDropDelta + probe.supportDropCount,
                probe.pairLower, probe.pairUpper, probe.orphanLower,
                probe.orphanLowerDropId, probe.orphanLowerDropCount,
                probe.orphanUpper, probe.orphanUpperDropDelta, probe.supportLower,
                probe.supportUpper, probe.supportDropId, probe.supportDropCount);
        return new IronDoorSubsystemObservation(probe.domains(), probe.lifecycle(),
                probe.physics(), probe.timing(), probe.neighbors());
    }
    void emit() {
        trace.emitTo(System.out);
    }
}
