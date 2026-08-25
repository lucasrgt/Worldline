package worldline.smoke.iceformationb173;

import net.minecraft.src.*;

/** Exposes one native ambient block pass and normalizes the first cold candidate. */
final class IceWorld extends World {
  private int coldX;
  private int coldZ;
  private int minimumX;
  private int minimumZ;
  IceWorld(ISaveHandler handler, String name, long seed) {
    super(handler, name, seed, null);
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
    playerEntities.add(new IcePlayer(this, centerX * 16, centerZ * 16));
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
      throw new IllegalStateException("seed has no cold biome in active radius");
  }
  void ambientPass() {
    doRandomUpdateTicks();
  }
  int[] observation() {
    int x = coldX, z = coldZ;
    for (int scanX = minimumX; scanX < minimumX + 304; scanX++)
      for (int scanZ = minimumZ; scanZ < minimumZ + 304; scanZ++)
        if (getBlockId(scanX, 64, scanZ) == Block.ice.blockID) {
          x = scanX;
          z = scanZ;
        }
    return new int[] {getBlockId(x, 64, z), getBlockMetadata(x, 64, z),
        getWorldChunkManager().getBiomeGenAt(x, z).getEnableSnow() ? 1 : 0,
        getSavedLightValue(EnumSkyBlock.Block, x, 65, z)};
  }
  int iceCount() {
    int count = 0;
    for (int x = minimumX; x < minimumX + 304; x++)
      for (int z = minimumZ; z < minimumZ + 304; z++)
        if (getBlockId(x, 64, z) == Block.ice.blockID)
          count++;
    return count;
  }
}
