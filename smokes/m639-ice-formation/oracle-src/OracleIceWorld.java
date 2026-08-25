/** Official-name world exposing one ambient block pass. */
final class OracleIceWorld extends dj {
  private int coldX;
  private int coldZ;
  private int minimumX;
  private int minimumZ;
  OracleIceWorld(om handler, String name, long seed) {
    super(handler, name, seed, null);
  }
  @SuppressWarnings("unchecked")
  void prepare() {
    int centerX = 0, centerZ = 0;
    boolean found = false;
    for (int cx = -128; cx <= 128 && !found; cx++)
      for (int cz = -128; cz <= 128; cz++)
        if (a().a(cx * 16 + 8, cz * 16 + 8).c()) {
          centerX = cx;
          centerZ = cz;
          found = true;
          break;
        }
    if (!found)
      throw new IllegalStateException("seed has no cold biome in search boundary");
    minimumX = (centerX - 9) * 16;
    minimumZ = (centerZ - 9) * 16;
    for (int cx = centerX - 9; cx <= centerX + 9; cx++)
      for (int cz = centerZ - 9; cz <= centerZ + 9; cz++) c(cx, cz);
    d.add(new OracleIcePlayer(this, centerX * 16, centerZ * 16));
    found = false;
    for (int x = minimumX; x < minimumX + 304 && !found; x++)
      for (int z = minimumZ; z < minimumZ + 304; z++)
        if (a().a(x, z).c()) {
          coldX = x;
          coldZ = z;
          found = true;
          break;
        }
    if (!found)
      throw new IllegalStateException("seed has no cold biome in active radius");
  }
  void ambientPass() {
    j();
  }
  int[] observation() {
    int x = coldX, z = coldZ;
    for (int scanX = minimumX; scanX < minimumX + 304; scanX++)
      for (int scanZ = minimumZ; scanZ < minimumZ + 304; scanZ++)
        if (a(scanX, 64, scanZ) == na.aU.bn) {
          x = scanX;
          z = scanZ;
        }
    return new int[] {a(x, 64, z), c(x, 64, z), a().a(x, z).c() ? 1 : 0, a(co.b, x, 65, z)};
  }
  int iceCount() {
    int count = 0;
    for (int x = minimumX; x < minimumX + 304; x++)
      for (int z = minimumZ; z < minimumZ + 304; z++)
        if (a(x, 64, z) == na.aU.bn)
          count++;
    return count;
  }
}
