import worldline.trace.CanonicalTrace;

/** Executes paired native snowfall and dry schedulers directly against the official JAR. */
public final class WorldlineSnowAccumulationOracle {
  private static final long SEED = 1772835215L;
  private static final int MAXIMUM_PASSES = 16;
  private WorldlineSnowAccumulationOracle() {}
  public static void main(String[] arguments) {
    OracleSnowWorld wet = world(true, "snowfall"), dry = world(false, "dry");
    int[] wetState = wet.observation(), dryState = dry.observation();
    int pass = 0;
    while (wetState[0] != na.aT.bn && pass < MAXIMUM_PASSES) {
      pass++;
      wet.ambientPass();
      dry.ambientPass();
      wetState = wet.observation();
      dryState = dry.observation();
      require(wetState[2] == 1 && dryState[2] == 1, "official snow cell left cold biome");
      require(dryState[3] == 0 && dryState[0] == 0, "official dry control changed");
      require(wetState[0] == 0 || wetState[0] == na.aT.bn,
          "official snowfall cell changed unexpectedly");
    }
    require(wetState[0] == na.aT.bn && wetState[3] == 1 && wetState[4] < 10,
        "official snow layer absent after bounded passes");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    trace.record("accumulated", 0L, 0, 0, na.aT.bn, wetState[4], MAXIMUM_PASSES, 1, 0);
    trace.emitTo(System.out);
  }
  private static OracleSnowWorld world(boolean snowfall, String name) {
    OracleSnowWorld world = new OracleSnowWorld(
        new OracleSnowMemorySaveHandler(SEED, name, snowfall), name, SEED, snowfall);
    world.prepare();
    world.r.setSeed(SEED);
    require(world.observation()[0] == 0, "official fixture did not begin as air");
    return world;
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
