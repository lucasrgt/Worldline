package worldline.smoke.weathermatrixb173;

import java.io.IOException;
import net.minecraft.src.*;

/** Fails closed if the weather-only fixture unexpectedly requests terrain. */
final class EmptyChunkLoader implements IChunkLoader {
  public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
    throw new IOException("weather fixture requested a chunk");
  }
  public void saveChunk(World world, Chunk chunk) throws IOException { }
  public void saveExtraChunkData(World world, Chunk chunk) throws IOException { }
  public void func_661_a() { }
  public void saveExtraData() { }
}
