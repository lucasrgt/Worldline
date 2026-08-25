package worldline.smoke.naturalwolfpackb173;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.src.*;

/** Hosts one fresh natural peaceful-spawn attempt on a forest or taiga grass matrix. */
final class WolfPackWorld extends World {
  WolfPackWorld(ISaveHandler handler, String name, long seed) {
    super(handler, name, seed, null);
  }
  @SuppressWarnings("unchecked")
  void prepare() {
    int centerX = 0, centerZ = 0;
    boolean found = false;
    for (int cx = -128; cx <= 128 && !found; cx++)
      for (int cz = -128; cz <= 128; cz++) {
        BiomeGenBase biome = getWorldChunkManager().getBiomeGenAt(cx * 16 + 8, cz * 16 + 8);
        if (biome == BiomeGenBase.forest || biome == BiomeGenBase.taiga) {
          centerX = cx;
          centerZ = cz;
          found = true;
          break;
        }
      }
    if (!found)
      throw new IllegalStateException("seed has no wolf biome in search boundary");
    for (int cx = centerX - 8; cx <= centerX + 8; cx++)
      for (int cz = centerZ - 8; cz <= centerZ + 8; cz++) getChunkFromChunkCoords(cx, cz);
    playerEntities.add(new WolfPlayer(this, centerX * 16, centerZ * 16));
  }
  void spawn(long randomSeed) {
    rand.setSeed(randomSeed);
    SpawnerAnimals.performSpawning(this, false, true);
  }
  @SuppressWarnings("rawtypes")
  int coherentPackSize() {
    List<EntityWolf> wolves = new ArrayList<>();
    for (Object entity : loadedEntityList)
      if (entity instanceof EntityWolf)
        wolves.add((EntityWolf) entity);
    int best = 0;
    for (EntityWolf anchor : wolves) {
      int nearby = 0;
      for (EntityWolf wolf : wolves) {
        double dx = anchor.posX - wolf.posX, dz = anchor.posZ - wolf.posZ;
        if (dx * dx + dz * dz <= 24D * 24D)
          nearby++;
      }
      best = Math.max(best, Math.min(nearby, 8));
    }
    return best;
  }
}
