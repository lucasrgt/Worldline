import worldline.trace.CanonicalTrace;

/** Executes native lightning ignition directly against the official server JAR. */
public final class WorldlineLightningFireOracle {
  private static final long SEED = 58920260824L;
  private static final int TICKS = 2;
  private static final int X = 8;
  private static final int Y = 65;
  private static final int Z = 8;

  private WorldlineLightningFireOracle() { }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "normal", 2);
    run(trace, "easy", 1);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, int difficulty) {
    dj world = new dj(new OracleLightningMemorySaveHandler(SEED, label), label, SEED, null);
    world.q = difficulty;
    world.r.setSeed(SEED);
    for (int chunkX = -2; chunkX <= 2; chunkX++)
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.c(chunkX, chunkZ);
    require(world.a(X, Y - 1, Z) == na.u.bn && world.a(X, Y, Z) == 0,
        "official lightning fixture drifted");
    snapshot(trace, label + "-seed", world, difficulty, null);
    OracleSeededLightning lightning =
        new OracleSeededLightning(world, X + 0.5D, Y, Z + 0.5D, SEED);
    require(world.b(lightning), "official lightning entity was rejected");
    snapshot(trace, label + "-strike", world, difficulty, lightning);
    for (int tick = 1; tick <= TICKS; tick++) {
      world.h();
      world.e();
      snapshot(trace, label + "-tick" + tick, world, difficulty, lightning);
    }
    int expected = difficulty >= 2 ? na.as.bn : 0;
    require(world.a(X, Y, Z) == expected, "official center ignition drifted");
    require(lightning.bw == TICKS && !lightning.bh, "official lightning lifecycle drifted");
  }

  private static void snapshot(CanonicalTrace trace, String label, dj world, int difficulty,
      OracleSeededLightning lightning) {
    trace.record(label, world.m(), world.b.size(), difficulty, world.a(X, Y, Z),
        world.c(X, Y, Z), lightning == null ? -1 : lightning.bw,
        lightning != null && lightning.bh ? 1 : 0);
  }
  private static void require(boolean condition, String message) {
    if (!condition) throw new IllegalStateException(message);
  }
}
