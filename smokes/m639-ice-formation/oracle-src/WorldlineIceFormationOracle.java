import worldline.trace.CanonicalTrace;

/** Executes paired native ice-formation schedulers directly against the official JAR. */
public final class WorldlineIceFormationOracle {
  private static final long SEED = 1772835215L;
  private static final int MAXIMUM_PASSES = 16;
  private WorldlineIceFormationOracle() {}
  public static void main(String[] arguments) {
    OracleIceWorld dark = world(false, "ice-dark");
    OracleIceWorld lit = world(true, "ice-lit");
    int[] darkState = dark.observation(), litState = lit.observation();
    int pass = 0;
    while (darkState[0] != na.aU.bn && pass < MAXIMUM_PASSES) {
      pass++;
      dark.ambientPass();
      lit.ambientPass();
      darkState = dark.observation();
      litState = lit.observation();
      require(darkState[2] == 1 && litState[2] == 1, "formation cell left cold biome");
      require(litState[3] >= 10 && litState[0] == na.C.bn, "official lit control water changed");
      require(darkState[0] == na.C.bn || darkState[0] == na.aU.bn,
          "official dark water changed unexpectedly");
    }
    require(
        darkState[0] == na.aU.bn && darkState[3] < 10, "official ice absent after bounded passes");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    trace.record(
        "formed", 0L, 0, na.C.bn, na.aU.bn, darkState[3], litState[3], MAXIMUM_PASSES, 1, 0);
    trace.emitTo(System.out);
  }
  private static OracleIceWorld world(boolean lit, String name) {
    OracleIceWorld world =
        new OracleIceWorld(new OracleIceMemorySaveHandler(SEED, name, lit), name, SEED);
    world.r.setSeed(SEED);
    world.prepare();
    require(world.observation()[0] == na.C.bn, "official fixture did not begin as water");
    return world;
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
