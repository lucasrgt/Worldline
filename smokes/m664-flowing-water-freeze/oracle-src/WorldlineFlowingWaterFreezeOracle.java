import worldline.trace.CanonicalTrace;

/** Executes the native cold-biome freeze boundary against the official server JAR. */
public final class WorldlineFlowingWaterFreezeOracle {
  private static final long SEED = 1772835215L;
  private static final int MAXIMUM_PASSES = 16;

  private WorldlineFlowingWaterFreezeOracle() {
  }

  public static void main(String[] arguments) {
    OracleFlowingWaterWorld world = new OracleFlowingWaterWorld(
        new OracleFlowingWaterMemorySaveHandler(SEED, "flowing-water-freeze"),
        "flowing-water-freeze", SEED);
    world.r.setSeed(SEED);
    world.prepare();
    int[] state = world.observation();
    require(state[0] == na.C.bn && state[1] == 0,
        "official freeze fixture did not begin with still water");
    require(state[2] == na.B.bn && state[3] == OracleFlowingWaterWorld.FLOWING_METADATA,
        "official flowing-water control was not installed");
    int pass = 0;
    while (state[0] != na.aU.bn && pass < MAXIMUM_PASSES) {
      pass++;
      world.ambientPass();
      state = world.observation();
      require(state[4] == 1 && state[5] < 10 && state[6] < 10,
          "official freeze cells left the cold low-light boundary");
      require(state[2] == na.B.bn && state[3] == OracleFlowingWaterWorld.FLOWING_METADATA,
          "official flowing water froze or changed level");
      require(state[0] == na.C.bn || state[0] == na.aU.bn,
          "official still water changed unexpectedly");
    }
    require(state[0] == na.aU.bn && state[1] == 0,
        "official still water did not freeze after bounded passes");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    trace.record("still-freeze-flowing-stays", 0L, 0, na.C.bn, na.aU.bn, na.B.bn,
        OracleFlowingWaterWorld.FLOWING_METADATA, state[4], state[5], state[6],
        MAXIMUM_PASSES);
    trace.emitTo(System.out);
  }

  private static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
