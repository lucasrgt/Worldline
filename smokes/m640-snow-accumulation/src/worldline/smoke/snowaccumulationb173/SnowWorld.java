package worldline.smoke.snowaccumulationb173;

import net.minecraft.src.*;

/** Exposes native weather priming and one ambient block pass. */
final class SnowWorld extends World {
  private final boolean snowfall;
  private int coldX, coldZ, minimumX, minimumZ;
  SnowWorld(ISaveHandler handler, String name, long seed, boolean snowfall) {
    super(handler, name, seed, null);
    this.snowfall = snowfall;
  }
  @SuppressWarnings("unchecked")
  void prepare() {
    int centerX = 0, centerZ = 0;
    boolean found = false;
    for (int cx = -128; cx <= 128 && !found; cx++)
      for (int cz = -128; cz <= 128; cz++)
        if (getWorldChunkManager().getBiomeGenAt(cx * 16 + 8, cz * 16 + 8).getEnableSnow()) {
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
      for (int cz = centerZ - 9; cz <= centerZ + 9; cz++) getChunkFromChunkCoords(cx, cz);
    playerEntities.add(new SnowPlayer(this, centerX * 16, centerZ * 16));
    found = false;
    for (int x = minimumX; x < minimumX + 304 && !found; x++)
      for (int z = minimumZ; z < minimumZ + 304; z++)
        if (getWorldChunkManager().getBiomeGenAt(x, z).getEnableSnow()) {
          coldX = x;
          coldZ = z;
          found = true;
          break;
        }
    if (!found)
      throw new IllegalStateException("active radius has no cold biome");
    for (int step = 0; step < 25; step++) updateWeather();
  }
  void ambientPass() {
    doRandomUpdateTicks();
  }
  int[] observation() {
    int x = coldX, z = coldZ;
    for (int scanX = minimumX; scanX < minimumX + 304; scanX++)
      for (int scanZ = minimumZ; scanZ < minimumZ + 304; scanZ++)
        if (getBlockId(scanX, 65, scanZ) == Block.snow.blockID) {
          x = scanX;
          z = scanZ;
        }
    return new int[] {getBlockId(x, 65, z), getBlockMetadata(x, 65, z),
        getWorldChunkManager().getBiomeGenAt(x, z).getEnableSnow() ? 1 : 0, snowfall ? 1 : 0,
        getSavedLightValue(EnumSkyBlock.Block, x, 65, z)};
  }
}
