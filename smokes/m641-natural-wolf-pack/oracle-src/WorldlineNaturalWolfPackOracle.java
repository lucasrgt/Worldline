import worldline.trace.CanonicalTrace;

/** Executes bounded native peaceful spawning directly against the official server JAR. */
public final class WorldlineNaturalWolfPackOracle {
  private static final long WORLD_SEED = 1772835215L;
  private static final long RANDOM_SEED = 64120260824L;
  private static final int MAXIMUM_ATTEMPTS = 64;
  private WorldlineNaturalWolfPackOracle() {}
  public static void main(String[] arguments) {
    int packSize = 0;
    for (int attempt = 1; attempt <= MAXIMUM_ATTEMPTS && packSize < 2; attempt++) {
      String name = "wolf-pack-" + attempt;
      OracleWolfPackWorld world = new OracleWolfPackWorld(
          new OracleWolfMemorySaveHandler(WORLD_SEED, name), name, WORLD_SEED);
      world.prepare();
      world.spawn(RANDOM_SEED + attempt);
      packSize = world.coherentPackSize();
    }
    require(packSize >= 2 && packSize <= 8, "official natural wolf pack absent");
    CanonicalTrace trace = new CanonicalTrace(WORLD_SEED);
    trace.record("pack", 0L, 0, 95, 2, 8, MAXIMUM_ATTEMPTS, 1);
    trace.emitTo(System.out);
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
