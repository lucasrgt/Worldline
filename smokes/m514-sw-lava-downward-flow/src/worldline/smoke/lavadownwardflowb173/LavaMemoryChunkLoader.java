package worldline.smoke.lavadownwardflowb173;

import java.io.IOException;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.World;

/** Supplies one isolated vertical fluid column in three floor variants. */
final class LavaMemoryChunkLoader implements IChunkLoader {
    static final int OPEN = 0;
    static final int BLOCKED = 1;
    static final int SHAFT = 2;
    private static final int HEIGHT = 128;
    private final int fixture;

    LavaMemoryChunkLoader(int fixture) { this.fixture = fixture; }

    public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
        byte[] blocks = new byte[16 * HEIGHT * 16];
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            for (int y = 0; y <= 64; y++) set(blocks, x, y, z,
                    y == 0 ? Block.bedrock.blockID : Block.stone.blockID);
        }
        if (chunkX == 0 && chunkZ == 0) buildColumn(blocks);
        Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
        chunk.isTerrainPopulated = true;
        chunk.neverSave = true;
        chunk.func_353_b();
        return chunk;
    }

    private void buildColumn(byte[] blocks) {
        for (int y = 61; y <= 68; y++) for (int x = 7; x <= 9; x++) {
            for (int z = 7; z <= 9; z++) {
                if (x != 8 || z != 8) set(blocks, x, y, z, Block.stone.blockID);
            }
        }
        if (fixture == SHAFT) for (int y = 61; y <= 67; y++) set(blocks, 8, y, 8, 0);
        if (fixture == BLOCKED) set(blocks, 8, 67, 8, Block.stone.blockID);
    }

    private static void set(byte[] blocks, int x, int y, int z, int id) {
        blocks[x << 11 | z << 7 | y] = (byte) id;
    }
    public void saveChunk(World world, Chunk chunk) throws IOException { }
    public void saveExtraChunkData(World world, Chunk chunk) throws IOException { }
    public void func_661_a() { }
    public void saveExtraData() { }
}
