package worldline.smoke.iceformationb173;

import java.io.IOException;
import net.minecraft.src.*;

/** Supplies identical still-water surfaces, optionally pinned above the light boundary. */
final class IceMemoryChunkLoader implements IChunkLoader {
  private static final int HEIGHT = 128;
  private final boolean lit;
  IceMemoryChunkLoader(boolean lit) {
    this.lit = lit;
  }
  public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++) {
        for (int y = 0; y < 64; y++)
          set(blocks, x, y, z, y == 0 ? Block.bedrock.blockID : Block.stone.blockID);
        set(blocks, x, 64, z, Block.waterStill.blockID);
      }
    Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
    chunk.isTerrainPopulated = true;
    chunk.neverSave = true;
    chunk.func_353_b();
    if (lit)
      for (int x = 0; x < 16; x++)
        for (int z = 0; z < 16; z++) chunk.setLightValue(EnumSkyBlock.Block, x, 65, z, 15);
    return chunk;
  }
  private static void set(byte[] blocks, int x, int y, int z, int id) {
    blocks[x << 11 | z << 7 | y] = (byte) id;
  }
  public void saveChunk(World world, Chunk chunk) throws IOException {}
  public void saveExtraChunkData(World world, Chunk chunk) throws IOException {}
  public void func_661_a() {}
  public void saveExtraData() {}
}
