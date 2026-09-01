package worldline.smoke.b173repeater;

import java.io.IOException;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.World;

/** Deterministic superflat chunks without filesystem persistence. */
final class RepeaterMemoryChunkLoader implements IChunkLoader {
    public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
        byte[] blocks = new byte[16 * 128 * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= 64; y++)
                    blocks[x << 11 | z << 7 | y] = (byte) (y == 0
                            ? Block.bedrock.blockID : Block.stone.blockID);
            }
        }
        Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
        chunk.isTerrainPopulated = true;
        chunk.neverSave = true;
        chunk.func_353_b();
        return chunk;
    }
    public void saveChunk(World world, Chunk chunk) throws IOException {
    }
    public void saveExtraChunkData(World world, Chunk chunk) throws IOException {
    }
    public void func_661_a() {
    }
    public void saveExtraData() {
    }
}
