package worldline.smoke.inputs;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Floor lever ON plus a 20-tick pressed side button. */
final class LeverButtonBackend implements GameBackend {
    private static final int Y = 65;
    private static final int LEVER_X = 8;
    private static final int LEVER_Z = 8;
    private static final int BUTTON_X = 8;
    private static final int BUTTON_Z = 10;
    private static final int WALL_X = 7;

    private final long seed;
    private World world;

    LeverButtonBackend(long seed) {
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
        require(world.getBlockId(LEVER_X, 64, LEVER_Z) == Block.stone.blockID, "stone missing");
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
        require(current.setBlockAndMetadataWithNotify(LEVER_X, Y, LEVER_Z,
                Block.lever.blockID, 13), "lever placement failed");
        require(current.setBlockWithNotify(LEVER_X + 1, Y, LEVER_Z, Block.redstoneWire.blockID),
                "lever wire placement failed");
        require(current.setBlockWithNotify(WALL_X, Y, BUTTON_Z, Block.stone.blockID),
                "button wall placement failed");
        require(current.setBlockAndMetadataWithNotify(BUTTON_X, Y, BUTTON_Z,
                Block.button.blockID, 9), "button placement failed");
        require(current.setBlockWithNotify(BUTTON_X + 1, Y, BUTTON_Z, Block.redstoneWire.blockID),
                "button wire placement failed");
        current.scheduleUpdateTick(BUTTON_X, Y, BUTTON_Z, Block.button.blockID, 20);
    }

    void snapshot(CanonicalTrace trace, String label) {
        World current = requireWorld();
        trace.record(label, current.getWorldTime(), current.loadedEntityList.size(),
                current.getBlockMetadata(LEVER_X + 1, Y, LEVER_Z),
                current.getBlockMetadata(BUTTON_X, Y, BUTTON_Z),
                current.getBlockMetadata(BUTTON_X + 1, Y, BUTTON_Z));
    }

    void assertPlacedState() {
        World current = requireWorld();
        require(current.getBlockMetadata(LEVER_X + 1, Y, LEVER_Z) > 0, "lever wire dark");
        require((current.getBlockMetadata(BUTTON_X, Y, BUTTON_Z) & 8) != 0, "button not pressed");
        require(current.getBlockMetadata(BUTTON_X + 1, Y, BUTTON_Z) > 0, "button wire dark");
    }

    void assertFinalState() {
        World current = requireWorld();
        require(current.getBlockMetadata(LEVER_X + 1, Y, LEVER_Z) > 0, "lever released");
        require((current.getBlockMetadata(BUTTON_X, Y, BUTTON_Z) & 8) == 0, "button stayed pressed");
        require(current.getBlockMetadata(BUTTON_X + 1, Y, BUTTON_Z) == 0, "button wire stayed powered");
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
