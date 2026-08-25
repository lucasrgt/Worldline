/** Official-name world exposing weather priming and one ambient pass. */
final class OracleSnowWorld extends dj {
  private final boolean snowfall;
  private int coldX, coldZ, minimumX, minimumZ;
  OracleSnowWorld(om handler, String name, long seed, boolean snowfall) {
    super(handler, name, seed, null);
    this.snowfall = snowfall;
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
    d.add(new OracleSnowPlayer(this, centerX * 16, centerZ * 16));
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
      throw new IllegalStateException("active radius has no cold biome");
    for (int step = 0; step < 25; step++) i();
  }
  void ambientPass() {
    j();
  }
  int[] observation() {
    int x = coldX, z = coldZ;
    for (int scanX = minimumX; scanX < minimumX + 304; scanX++)
      for (int scanZ = minimumZ; scanZ < minimumZ + 304; scanZ++)
        if (a(scanX, 65, scanZ) == na.aT.bn) {
          x = scanX;
          z = scanZ;
        }
    return new int[] {
        a(x, 65, z), c(x, 65, z), a().a(x, z).c() ? 1 : 0, snowfall ? 1 : 0, a(co.b, x, 65, z)};
  }
}
