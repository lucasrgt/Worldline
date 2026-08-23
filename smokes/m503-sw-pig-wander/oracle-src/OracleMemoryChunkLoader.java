/** Official-name deterministic flat chunks with an optional tight cage. */
final class OracleMemoryChunkLoader implements an {
  private static final int HEIGHT = 128;
  private final boolean caged;
  OracleMemoryChunkLoader(boolean caged) {
    this.caged = caged;
  }

  public hi a(dj world, int chunkX, int chunkZ) {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        for (int y = 0; y <= 64; y++)
          set(blocks, x, y, z, y == 0 ? na.A.bn : na.u.bn);
      }
    }
    if (caged && chunkX == 0 && chunkZ == 0) {
      for (int y = 65; y <= 66; y++) {
        for (int x = 7; x <= 9; x++) {
          for (int z = 7; z <= 9; z++) {
            if (x != 8 || z != 8)
              set(blocks, x, y, z, na.u.bn);
          }
        }
      }
    }
    hi chunk = new hi(world, blocks, chunkX, chunkZ);
    chunk.n = true;
    chunk.p = true;
    chunk.b();
    return chunk;
  }

  private static void set(byte[] blocks, int x, int y, int z, int id) {
    blocks[x << 11 | z << 7 | y] = (byte) id;
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
