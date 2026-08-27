/** Official-name world exposing the native ambient pass over a water pair. */
final class OracleFlowingWaterWorld extends dj {
  static final int FLOWING_METADATA = 1;
  private static final int SURFACE_Y = 64;
  private int stillX;
  private int stillZ;
  private int flowingX;
  private int flowingZ;
  private int minimumX;
  private int minimumZ;

  OracleFlowingWaterWorld(om handler, String name, long seed) {
    super(handler, name, seed, null);
  }

  @SuppressWarnings("unchecked")
  void prepare() {
    int centerX = 0;
    int centerZ = 0;
    boolean found = false;
    for (int cx = -128; cx <= 128 && !found; cx++) {
      for (int cz = -128; cz <= 128; cz++) {
        if (a().a(cx * 16 + 8, cz * 16 + 8).c()) {
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
        c(cx, cz);
      }
    }
    d.add(new OracleFlowingWaterPlayer(this, centerX * 16, centerZ * 16));
    found = false;
    for (int x = minimumX; x < minimumX + 303 && !found; x++) {
      for (int z = minimumZ; z < minimumZ + 304; z++) {
        if (cold(x, z) && cold(x + 1, z)) {
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
    require(a(flowingX, SURFACE_Y, flowingZ, na.B.bn, FLOWING_METADATA),
        "official flowing-water control placement failed");
  }

  void ambientPass() {
    j();
  }

  int[] observation() {
    int observedX = stillX;
    int observedZ = stillZ;
    for (int x = minimumX; x < minimumX + 304; x++) {
      for (int z = minimumZ; z < minimumZ + 304; z++) {
        if (a(x, SURFACE_Y, z) == na.aU.bn) {
          observedX = x;
          observedZ = z;
        }
      }
    }
    return new int[] {a(observedX, SURFACE_Y, observedZ), c(observedX, SURFACE_Y, observedZ),
        a(flowingX, SURFACE_Y, flowingZ), c(flowingX, SURFACE_Y, flowingZ),
        cold(observedX, observedZ) && cold(flowingX, flowingZ) ? 1 : 0,
        a(co.b, observedX, SURFACE_Y + 1, observedZ),
        a(co.b, flowingX, SURFACE_Y + 1, flowingZ)};
  }

  private boolean cold(int x, int z) {
    return a().a(x, z).c();
  }

  private static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}

final class OracleFlowingWaterPlayer extends em {
  OracleFlowingWaterPlayer(dj world, int x, int z) {
    super(world);
    c(x + 0.5D, 66D, z + 0.5D, 0F, 0F);
  }
}
