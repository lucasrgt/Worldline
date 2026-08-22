package worldline.smoke.repeater;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Bridges the product runtime port to one delay-1 repeater fixture. */
final class RepeaterWorldBackend implements GameBackend {
    private static final int TORCH_X = 8;
    private static final int REPEATER_X = 9;
    private static final int WIRE_X = 10;
    private static final int OBSERVE_X = 11;
    private static final int Y = 65;
    private static final int Z = 8;
    private static final int FACE_EAST = 4;

    private final long seed;
    private World world;

    RepeaterWorldBackend(long seed) {
        this.seed = seed;
    }

    @Override
    public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void loadWorld(WorldSource source) {
        String worldName = source.path().getFileName().toString();
        world = new World(new MemorySaveHandler(seed, worldName), worldName, seed, null);
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                world.getChunkFromChunkCoords(chunkX, chunkZ);
            }
        }
        require(world.getBlockId(TORCH_X, 64, Z) == Block.stone.blockID, "fixture stone missing");
        require(world.getBlockId(TORCH_X, Y, Z) == 0, "torch cell is not air");
        require(world.getBlockId(REPEATER_X, Y, Z) == 0, "repeater cell is not air");
        require(world.getBlockId(WIRE_X, Y, Z) == 0, "wire cell is not air");
    }

    @Override
    public void tick() {
        requireWorld().tick();
    }

    @Override
    public void close() {
        world = null;
    }

    void placeCircuit() {
        World current = requireWorld();
        require(current.setBlockAndMetadataWithNotify(TORCH_X, Y, Z,
                Block.torchRedstoneActive.blockID, 5), "torch placement failed");
        require(current.setBlockAndMetadataWithNotify(REPEATER_X, Y, Z,
                Block.redstoneRepeaterIdle.blockID, 1), "repeater placement failed");
        require(current.setBlockWithNotify(WIRE_X, Y, Z, Block.redstoneWire.blockID),
                "wire placement failed");
    }

    void snapshot(CanonicalTrace trace, String label) {
        World current = requireWorld();
        int repeater = current.getBlockId(REPEATER_X, Y, Z);
        int powering = poweringTo(current, repeater);
        int powered = current.isBlockIndirectlyGettingPowered(OBSERVE_X, Y, Z) ? 1 : 0;
        trace.record(label, current.getWorldTime(), current.loadedEntityList.size(),
                repeater, current.getBlockId(WIRE_X, Y, Z),
                current.getBlockMetadata(WIRE_X, Y, Z), powering, powered);
    }

    void assertPlacedState() {
        World current = requireWorld();
        require(current.getBlockId(REPEATER_X, Y, Z) == Block.redstoneRepeaterIdle.blockID,
                "repeater locked on during placement");
        require(current.getBlockMetadata(WIRE_X, Y, Z) == 0, "wire powered during placement");
        require(poweringTo(current, current.getBlockId(REPEATER_X, Y, Z)) == 0,
                "repeater output live during placement");
    }

    void assertFinalState() {
        World current = requireWorld();
        require(current.getBlockId(REPEATER_X, Y, Z) == Block.redstoneRepeaterActive.blockID,
                "repeater stayed idle");
        require(current.getBlockId(WIRE_X, Y, Z) == Block.redstoneWire.blockID, "wire missing");
        require(current.getBlockMetadata(WIRE_X, Y, Z) > 0, "wire has no power");
        require(poweringTo(current, current.getBlockId(REPEATER_X, Y, Z)) == 1,
                "repeater output is dark");
        require(current.isBlockIndirectlyGettingPowered(OBSERVE_X, Y, Z), "observer is unpowered");
        require(current.getBlockId(TORCH_X, 64, Z) == Block.stone.blockID, "fixture stone changed");
    }

    private static int poweringTo(World current, int repeater) {
        return repeater == Block.redstoneRepeaterActive.blockID
                && Block.redstoneRepeaterActive.isPoweringTo(current, REPEATER_X, Y, Z, FACE_EAST)
                ? 1 : 0;
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
