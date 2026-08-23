package worldline.smoke.randomblocks;
import java.io.IOException;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.World;
/** Deterministic stone floor for random block updates. */
final class MemoryChunkLoader implements IChunkLoader {
  public Chunk loadChunk(World w, int cx, int cz) throws IOException {
    byte[] b = new byte[16 * 128 * 16];
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y <= 64; y++)
          b[x << 11 | z << 7 | y] = (byte) (y == 0 ? Block.bedrock.blockID : Block.stone.blockID);
    Chunk c = new Chunk(w, b, cx, cz);
    c.isTerrainPopulated = true;
    c.neverSave = true;
    c.func_353_b();
    return c;
  }
  public void saveChunk(World w, Chunk c) throws IOException {
  }
  public void saveExtraChunkData(World w, Chunk c) throws IOException {
  }
  public void func_661_a() {
  }
  public void saveExtraData() {
  }
}
