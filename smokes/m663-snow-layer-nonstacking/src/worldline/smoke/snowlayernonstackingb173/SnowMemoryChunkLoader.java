package worldline.smoke.snowlayernonstackingb173;

import java.io.IOException;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.World;

/** Supplies a bounded solid surface with air available for one snow layer. */
final class SnowMemoryChunkLoader implements IChunkLoader {
    private static final int HEIGHT = 128;

    @Override
    public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
        byte[] blocks = new byte[16 * HEIGHT * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= 64; y++) {
                    set(blocks, x, y, z,
                            y == 0 ? Block.bedrock.blockID : Block.stone.blockID);
                }
            }
        }
        Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
        chunk.isTerrainPopulated = true;
        chunk.neverSave = true;
        chunk.func_353_b();
        return chunk;
    }

    private static void set(byte[] blocks, int x, int y, int z, int id) {
        blocks[x << 11 | z << 7 | y] = (byte) id;
    }

    @Override
    public void saveChunk(World world, Chunk chunk) throws IOException {
    }

    @Override
    public void saveExtraChunkData(World world, Chunk chunk) throws IOException {
    }

    @Override
    public void func_661_a() {
    }

    @Override
    public void saveExtraData() {
    }
}
