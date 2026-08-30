/** Official-name counterpart of the deterministic in-memory chunk boundary. */
final class OracleFluidFrozenMemoryChunkLoader implements an {
  private static final int HEIGHT = 128;
  private static final int STONE_TOP = 64;

  public hi a(dj world, int chunkX, int chunkZ) {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        for (int y = 0; y <= STONE_TOP; y++) {
          int index = x << 11 | z << 7 | y;
          blocks[index] = (byte) (y == 0 ? na.A.bn : na.u.bn);
        }
      }
    }
    hi chunk = new hi(world, blocks, chunkX, chunkZ);
    chunk.n = true;
    chunk.p = true;
    chunk.b();
    return chunk;
  }

  public void a(dj world, hi chunk) {
  }

  public void b(dj world, hi chunk) {
  }

  public void a() {
  }

  public void b() {
  }
}
