package worldline.smoke.lightingmatrixb173;

import java.io.IOException;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.World;

/** Supplies flat chunks with one roof and one closable skylight aperture. */
final class MemoryChunkLoader implements IChunkLoader {
  private static final int HEIGHT = 128;

  public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y <= 64; y++)
          set(blocks, x, y, z, y == 0 ? Block.bedrock.blockID : Block.stone.blockID);
    if (chunkX == 0 && chunkZ == 0) {
      roof(blocks, 10, 14, 10, 14, false);
      roof(blocks, 2, 6, 2, 6, true);
    }
    Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
    chunk.isTerrainPopulated = true;
    chunk.neverSave = true;
    chunk.func_353_b();
    return chunk;
  }

  private static void roof(byte[] blocks, int minX, int maxX, int minZ, int maxZ,
      boolean aperture) {
    for (int x = minX; x <= maxX; x++)
      for (int z = minZ; z <= maxZ; z++)
        if (!aperture || x != 4 || z != 4)
          set(blocks, x, 68, z, Block.stone.blockID);
  }

  private static void set(byte[] blocks, int x, int y, int z, int id) {
    blocks[x << 11 | z << 7 | y] = (byte) id;
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
