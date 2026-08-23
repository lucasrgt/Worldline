import worldline.trace.CanonicalTrace;

/** Executes fire support changes against the official server JAR. */
public final class WorldlineFireSupportOracle {
  private static final long SEED = 51520240820L;
  private static final int TICKS = 2;
  private static final int LOST = 0;
  private static final int RETAINED = 1;
  private static final int UNSUPPORTED = 2;
  private static final int X = 8;
  private static final int Z = 8;
  private static final int SUPPORT_Y = 64;
  private WorldlineFireSupportOracle() {
  }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "lost", LOST);
    run(trace, "retained", RETAINED);
    run(trace, "unsupported", UNSUPPORTED);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, int fixture) {
    dj world = new dj(
        new OracleFireMemorySaveHandler(SEED, "worldline-smoke"), "worldline-smoke", SEED, null);
    world.r.setSeed(SEED);
    preload(world);
    if (fixture == UNSUPPORTED)
      removeSupport(world);
    require(world.b(X, SUPPORT_Y + 1, Z, na.as.bn, 0), "fire placement did not change the cell");
    if (fixture == UNSUPPORTED)
      require(fireId(world) == 0, "unsupported fire survived placement");
    else
      require(fireId(world) == na.as.bn, "supported fire was not placed");
    snapshot(trace, label + "-seed", world);
    if (fixture == LOST)
      removeSupport(world);
    snapshot(trace, label + "-action", world);
    for (int tick = 1; tick <= TICKS; tick++) {
      world.h();
      snapshot(trace, label + "-tick" + tick, world);
    }
    if (fixture == RETAINED)
      require(supportId(world) == na.u.bn && fireId(world) == na.as.bn,
          "supported fire did not remain");
    else
      require(supportId(world) == 0 && fireId(world) == 0, "unsupported fire or support remained");
  }

  private static void preload(dj world) {
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.c(chunkX, chunkZ);
    }
  }
  private static void removeSupport(dj world) {
    if (supportId(world) == 0)
      return;
    require(world.e(X, SUPPORT_Y, Z, 0), "support removal failed");
  }
  private static void snapshot(CanonicalTrace trace, String label, dj world) {
    trace.record(label, world.m(), 0,
        new int[] {supportId(world), world.c(X, SUPPORT_Y, Z), fireId(world),
            world.c(X, SUPPORT_Y + 1, Z)});
  }
  private static int supportId(dj world) {
    return world.a(X, SUPPORT_Y, Z);
  }
  private static int fireId(dj world) {
    return world.a(X, SUPPORT_Y + 1, Z);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
