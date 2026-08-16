package worldline.smoke.b173;

import java.io.IOException;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.World;

/** Supplies deterministic superflat chunks without McRegion I/O. */
final class MemoryChunkLoader implements IChunkLoader {
    private static final int HEIGHT = 128;
    private static final int STONE_TOP = 64;

    public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
        byte[] blocks = new byte[16 * HEIGHT * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= STONE_TOP; y++) {
                    int index = x << 11 | z << 7 | y;
                    blocks[index] = (byte) (y == 0 ? Block.bedrock.blockID : Block.stone.blockID);
                }
            }
        }
        Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
        chunk.isTerrainPopulated = true;
        chunk.neverSave = true;
        chunk.func_353_b();
        return chunk;
    }

    public void saveChunk(World world, Chunk chunk) throws IOException {}

    public void saveExtraChunkData(World world, Chunk chunk) throws IOException {}

    public void func_661_a() {}

    public void saveExtraData() {}
}
