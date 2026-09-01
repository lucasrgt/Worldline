import java.util.HashMap;
import java.util.Map;

/** Official-name in-memory chunk persistence for moving-fluid reloads. */
final class OracleFlowingFluidMemoryChunkLoader implements an {
  private static final int HEIGHT = 128;
  private final Map<Long, Snapshot> saved = new HashMap<Long, Snapshot>();

  public hi a(dj world, int chunkX, int chunkZ) {
    Snapshot snapshot = saved.get(key(chunkX, chunkZ));
    byte[] blocks = snapshot == null ? terrain() : snapshot.blocks.clone();
    hi chunk = new hi(world, blocks, chunkX, chunkZ);
    if (snapshot != null)
      restoreMetadata(chunk, snapshot.metadata);
    chunk.n = true;
    chunk.p = false;
    chunk.b();
    return chunk;
  }

  public void a(dj world, hi chunk) {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    byte[] metadata = new byte[blocks.length];
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < HEIGHT; y++) {
          int index = index(x, y, z);
          blocks[index] = (byte) chunk.a(x, y, z);
          metadata[index] = (byte) chunk.b(x, y, z);
        }
    saved.put(key(chunk.j, chunk.k), new Snapshot(blocks, metadata));
  }

  public void b(dj world, hi chunk) {
  }

  public void a() {
  }

  public void b() {
  }

  private static byte[] terrain() {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y <= 64; y++)
          blocks[index(x, y, z)] = (byte) (y == 0 ? na.A.bn : na.u.bn);
    return blocks;
  }

  private static void restoreMetadata(hi chunk, byte[] metadata) {
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < HEIGHT; y++) {
          int value = metadata[index(x, y, z)] & 15;
          if (value != 0)
            chunk.b(x, y, z, value);
        }
  }

  private static int index(int x, int y, int z) {
    return x << 11 | z << 7 | y;
  }

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static final class Snapshot {
    final byte[] blocks;
    final byte[] metadata;

    Snapshot(byte[] blocks, byte[] metadata) {
      this.blocks = blocks;
      this.metadata = metadata;
    }
  }
}
