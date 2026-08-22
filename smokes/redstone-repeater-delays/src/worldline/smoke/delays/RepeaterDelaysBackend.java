package worldline.smoke.delays;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Three parallel delay-2/3/4 idle repeaters facing a torch. */
final class RepeaterDelaysBackend implements GameBackend {
    private static final int TORCH_X = 8;
    private static final int REPEATER_X = 9;
    private static final int Y = 65;
    private static final int[] Z = { 8, 9, 10 };
    private static final int[] META = { 5, 9, 13 };

    private final long seed;
    private World world;

    RepeaterDelaysBackend(long seed) {
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
        for (int index = 0; index < Z.length; index++) {
            require(world.getBlockId(TORCH_X, 64, Z[index]) == Block.stone.blockID, "stone missing");
            require(world.getBlockId(TORCH_X, Y, Z[index]) == 0, "torch cell is not air");
            require(world.getBlockId(REPEATER_X, Y, Z[index]) == 0, "repeater cell is not air");
        }
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
        for (int index = 0; index < Z.length; index++) {
            require(current.setBlockAndMetadataWithNotify(TORCH_X, Y, Z[index],
                    Block.torchRedstoneActive.blockID, 5), "torch placement failed");
            require(current.setBlockAndMetadataWithNotify(REPEATER_X, Y, Z[index],
                    Block.redstoneRepeaterIdle.blockID, META[index]), "repeater placement failed");
        }
    }

    void snapshot(CanonicalTrace trace, String label) {
        World current = requireWorld();
        trace.record(label, current.getWorldTime(), current.loadedEntityList.size(),
                current.getBlockId(REPEATER_X, Y, Z[0]), current.getBlockId(REPEATER_X, Y, Z[1]),
                current.getBlockId(REPEATER_X, Y, Z[2]));
    }

    void assertPlacedState() {
        for (int index = 0; index < Z.length; index++) {
            require(requireWorld().getBlockId(REPEATER_X, Y, Z[index])
                    == Block.redstoneRepeaterIdle.blockID, "repeater locked on during placement");
        }
    }

    void assertFinalState() {
        World current = requireWorld();
        require(current.getBlockId(REPEATER_X, Y, Z[0]) == Block.redstoneRepeaterActive.blockID,
                "delay-2 stayed idle");
        require(current.getBlockId(REPEATER_X, Y, Z[1]) == Block.redstoneRepeaterActive.blockID,
                "delay-3 stayed idle");
        require(current.getBlockId(REPEATER_X, Y, Z[2]) == Block.redstoneRepeaterActive.blockID,
                "delay-4 stayed idle");
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
