package worldline.smoke.entitycollisionresolutionb173;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Bridges the product runtime port to the mapped vanilla world used by this smoke. */
final class CollisionWorldBackend implements GameBackend {
    private static final int X = 8;
    private static final int Z = 8;
    private static final double Y = 65.0D;
    private static final double OVERLAP = 0.05D;
    private static final double SEPARATED = 2.0D;
    private static final long BOUND_MILLI = 4000L;
    private static final long EPSILON_MILLI = 10L;

    private final long seed;
    private final boolean overlap;
    private World world;
    private CollisionEntity first;
    private CollisionEntity second;
    private long beforeMilli;

    CollisionWorldBackend(long seed, boolean overlap) {
        this.seed = seed;
        this.overlap = overlap;
    }

    @Override
    public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new MemorySaveHandler(seed, name), name, seed, null);
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                world.getChunkFromChunkCoords(chunkX, chunkZ);
            }
        }
        require(world.getBlockId(X, 64, Z) == Block.stone.blockID, "fixture stone missing");
    }

    @Override
    public void tick() {
        World current = requireWorld();
        current.tick();
        current.updateEntities();
    }

    @Override
    public void close() {
        world = null;
        first = null;
        second = null;
    }

    void seed() {
        World current = requireWorld();
        first = new CollisionEntity(current);
        second = new CollisionEntity(current);
        double offset = overlap ? OVERLAP : SEPARATED;
        first.setPosition(X, Y, Z);
        second.setPosition(X + offset, Y, Z);
        require(current.entityJoinedWorld(first), "first fixture entity was rejected");
        require(current.entityJoinedWorld(second), "second fixture entity was rejected");
        beforeMilli = separationMilli();
    }

    void snapshot(CanonicalTrace trace, String label) {
        World current = requireWorld();
        trace.record(label, current.getWorldTime(), current.loadedEntityList.size(),
                (int) separationMilli());
    }

    void assertOutcome() {
        long afterMilli = separationMilli();
        if (overlap) {
            require(afterMilli > beforeMilli && afterMilli - beforeMilli <= BOUND_MILLI,
                    "overlap did not resolve to bounded horizontal push: "
                            + beforeMilli + " -> " + afterMilli);
        } else {
            require(Math.abs(afterMilli - beforeMilli) <= EPSILON_MILLI,
                    "separated entities drifted horizontally");
        }
    }

    private long separationMilli() {
        requireWorld();
        require(first != null && second != null, "living entities are not seeded");
        double dx = second.posX - first.posX;
        double dz = second.posZ - first.posZ;
        return Math.round(Math.sqrt(dx * dx + dz * dz) * 1000.0D);
    }

    private World requireWorld() {
        if (world == null) {
            throw new IllegalStateException("vanilla world is not loaded");
        }
        return world;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
