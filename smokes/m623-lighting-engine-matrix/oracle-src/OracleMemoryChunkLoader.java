/** Official-name flat chunks with one roof and one closable skylight aperture. */
final class OracleMemoryChunkLoader implements an {
  private static final int HEIGHT = 128;

  public hi a(dj world, int chunkX, int chunkZ) {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y <= 64; y++)
          set(blocks, x, y, z, y == 0 ? na.A.bn : na.u.bn);
    if (chunkX == 0 && chunkZ == 0) {
      roof(blocks, 10, 14, 10, 14, false);
      roof(blocks, 2, 6, 2, 6, true);
    }
    hi chunk = new hi(world, blocks, chunkX, chunkZ);
    chunk.n = true;
    chunk.p = true;
    chunk.b();
    return chunk;
  }

  private static void roof(byte[] blocks, int minX, int maxX, int minZ, int maxZ,
      boolean aperture) {
    for (int x = minX; x <= maxX; x++)
      for (int z = minZ; z <= maxZ; z++)
        if (!aperture || x != 4 || z != 4)
          set(blocks, x, 68, z, na.u.bn);
  }

  private static void set(byte[] blocks, int x, int y, int z, int id) {
    blocks[x << 11 | z << 7 | y] = (byte) id;
  }

  public void a(dj world, hi chunk) { }
  public void b(dj world, hi chunk) { }
  public void a() { }
  public void b() { }
}
