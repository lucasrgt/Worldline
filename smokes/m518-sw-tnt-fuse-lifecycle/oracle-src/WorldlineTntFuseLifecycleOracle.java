import worldline.trace.CanonicalTrace;

/** Executes the internal TNT-fuse fixture directly against the official server JAR. */
public final class WorldlineTntFuseLifecycleOracle {
  private static final long SEED = 51820240820L;
  private static final int X = 8, Y = 65, Z = 8;
  private WorldlineTntFuseLifecycleOracle() {
  }
  public static void main(String[] a) {
    System.setProperty("java.awt.headless", "true");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "positive", 81);
    run(trace, "unprimed", 81);
    run(trace, "mid", 40);
    trace.emitTo(System.out);
  }
  private static void run(CanonicalTrace trace, String mode, int ticks) {
    dj world =
        new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke"), "worldline-smoke", SEED, null);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        world.c(x, z);
    require(world.a(X, Y - 1, Z) == na.u.bn, "fixture stone missing");
    kg tnt = null;
    if (mode.equals("unprimed"))
      require(world.e(X, Y, Z, na.an.bn), "unprimed TNT placement failed");
    else {
      tnt = new kg(world, X + 0.5D, Y, Z + 0.5D);
      tnt.aS = 0D;
      tnt.aT = 0D;
      tnt.aU = 0D;
      require(tnt.a == 80 && world.b(tnt), "primed TNT seed failed");
    }
    snapshot(trace, mode + "-seed", world, tnt);
    for (int tick = 1; tick <= ticks; tick++) {
      world.h();
      world.e();
      if (tick == 1 || tick == 40 || tick == 79 || tick == 80 || tick == 81 || tick == ticks)
        snapshot(trace, mode + "-tick" + tick, world, tnt);
    }
    if (mode.equals("unprimed"))
      require(tnt == null && world.a(X, Y, Z) == na.an.bn, "unprimed TNT changed");
    else if (mode.equals("mid"))
      require(tnt.a == 40 && !tnt.bh && world.b.contains(tnt), "mid-fuse state drift");
    else
      require(tnt.a == -1 && tnt.bh && !world.b.contains(tnt), "terminal fuse state drift");
  }
  private static void snapshot(CanonicalTrace trace, String label, dj world, kg tnt) {
    boolean present = tnt != null && world.b.contains(tnt);
    trace.record(label, world.m(), present ? 1 : 0, tnt == null ? -999 : tnt.a,
        tnt != null && tnt.bh ? 1 : 0, world.a(X, Y, Z));
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
