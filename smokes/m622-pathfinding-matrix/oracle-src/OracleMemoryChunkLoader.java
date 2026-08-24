/** Official-name flat chunks with a gap wall or a sealed target ring. */
final class OracleMemoryChunkLoader implements an {
  private static final int HEIGHT = 128;
  private final int terrain;

  OracleMemoryChunkLoader(int terrain) {
    this.terrain = terrain;
  }

  public hi a(dj world, int chunkX, int chunkZ) {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y <= 64; y++)
          set(blocks, x, y, z, y == 0 ? na.A.bn : na.u.bn);
    if (chunkX == 0 && chunkZ == 0)
      obstacles(blocks);
    hi chunk = new hi(world, blocks, chunkX, chunkZ);
    chunk.n = true;
    chunk.p = true;
    chunk.b();
    return chunk;
  }

  private void obstacles(byte[] blocks) {
    for (int y = 65; y <= 66; y++) {
      if (terrain == 1)
        for (int z = 0; z <= 11; z++)
          set(blocks, 8, y, z, na.u.bn);
      if (terrain == 2)
        for (int x = 11; x <= 13; x++)
          for (int z = 7; z <= 9; z++)
            if (x != 12 || z != 8)
              set(blocks, x, y, z, na.u.bn);
    }
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
