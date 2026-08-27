package worldline.smoke.flowingwaterfreezeb173;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.World;

/** Exposes the native ambient pass over an adjacent still and flowing pair. */
final class FlowingWaterWorld extends World {
  private int stillX;
  private int stillZ;
  private int flowingX;
  private int flowingZ;
  private int minimumX;
  private int minimumZ;

  FlowingWaterWorld(FlowingWaterMemorySaveHandler handler, String name, long seed) {
    super(handler, name, seed, null);
  }

  @SuppressWarnings("unchecked")
  void prepare() {
    int centerX = 0;
    int centerZ = 0;
    boolean found = false;
    for (int cx = -128; cx <= 128 && !found; cx++) {
      for (int cz = -128; cz <= 128; cz++) {
        BiomeGenBase biome = getWorldChunkManager().getBiomeGenAt(cx * 16 + 8, cz * 16 + 8);
        if (biome.getEnableSnow()) {
          centerX = cx;
          centerZ = cz;
          found = true;
          break;
        }
      }
    }
    if (!found) {
      throw new IllegalStateException("seed has no cold biome in search boundary");
    }
    minimumX = (centerX - 9) * 16;
    minimumZ = (centerZ - 9) * 16;
    for (int cx = centerX - 9; cx <= centerX + 9; cx++) {
      for (int cz = centerZ - 9; cz <= centerZ + 9; cz++) {
        getChunkFromChunkCoords(cx, cz);
      }
    }
    playerEntities.add(new FlowingWaterPlayer(this, centerX * 16, centerZ * 16));
    found = false;
    for (int x = minimumX; x < minimumX + 303 && !found; x++) {
      for (int z = minimumZ; z < minimumZ + 304; z++) {
        boolean adjacent = cold(x, z) && cold(x + 1, z);
        if (adjacent) {
          stillX = x;
          stillZ = z;
          flowingX = x + 1;
          flowingZ = z;
          found = true;
          break;
        }
      }
    }
    if (!found) {
      throw new IllegalStateException("active radius has no adjacent cold cells");
    }
    require(setBlockAndMetadata(flowingX, SURFACE_Y, flowingZ, Block.waterMoving.blockID,
        FlowingWaterFreezeBackend.FLOWING_METADATA), "flowing-water control placement failed");
  }

  void ambientPass() {
    doRandomUpdateTicks();
  }

  int[] observation() {
    int observedX = stillX;
    int observedZ = stillZ;
    for (int x = minimumX; x < minimumX + 304; x++) {
      for (int z = minimumZ; z < minimumZ + 304; z++) {
        boolean ice = getBlockId(x, SURFACE_Y, z) == Block.ice.blockID;
        if (ice) {
          observedX = x;
          observedZ = z;
        }
      }
    }
    return new int[] {getBlockId(observedX, SURFACE_Y, observedZ),
        getBlockMetadata(observedX, SURFACE_Y, observedZ), getBlockId(flowingX, SURFACE_Y, flowingZ),
        getBlockMetadata(flowingX, SURFACE_Y, flowingZ), cold(observedX, observedZ)
            && cold(flowingX, flowingZ) ? 1 : 0,
        getSavedLightValue(EnumSkyBlock.Block, observedX, SURFACE_Y + 1, observedZ),
        getSavedLightValue(EnumSkyBlock.Block, flowingX, SURFACE_Y + 1, flowingZ)};
  }

  private static final int SURFACE_Y = 64;

  private boolean cold(int x, int z) {
    return getWorldChunkManager().getBiomeGenAt(x, z).getEnableSnow();
  }

  private static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
