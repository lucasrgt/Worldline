import worldline.trace.CanonicalTrace;

/** Executes the collision-resolution fixture directly against the official obfuscated server JAR. */
public final class WorldlineEntityCollisionOracle {
  private static final long SEED = 50220240820L;
  private static final int X = 8;
  private static final int Z = 8;
  private static final int TICKS = 10;
  private static final double Y = 65.0D;
  private static final double OVERLAP = 0.05D;
  private static final double SEPARATED = 2.0D;

  private WorldlineEntityCollisionOracle() {
  }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "overlap", true);
    run(trace, "separated", false);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, boolean overlap) {
    dj world =
        new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke"), "worldline-smoke", SEED, null);
    preload(world);
    require(world.a(X, 64, Z) == na.u.bn, "fixture stone missing");

    OracleCollisionEntity first = new OracleCollisionEntity(world);
    OracleCollisionEntity second = new OracleCollisionEntity(world);
    double offset = overlap ? OVERLAP : SEPARATED;
    first.c(X, Y, Z);
    second.c(X + offset, Y, Z);
    require(world.b(first), "first fixture entity was rejected");
    require(world.b(second), "second fixture entity was rejected");

    long before = milli(horizontal(first, second));
    snapshot(trace, label + "-seed", world, first, second);
    for (int tick = 1; tick <= TICKS; tick++) {
      world.h();
      world.e();
      snapshot(trace, label + "-tick" + tick, world, first, second);
    }
    long after = milli(horizontal(first, second));
    if (overlap) {
      require(after > before && after - before <= 4000L,
          "overlap did not resolve to bounded horizontal push: " + before + " -> " + after);
    } else {
      require(Math.abs(after - before) <= 10L, "separated entities drifted horizontally");
    }
  }

  private static void preload(dj world) {
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
        world.c(chunkX, chunkZ);
      }
    }
  }

  private static void snapshot(CanonicalTrace trace, String label, dj world, hl first, hl second) {
    trace.record(label, world.m(), world.b.size(), (int) milli(horizontal(first, second)));
  }

  private static double horizontal(hl first, hl second) {
    double dx = second.aP - first.aP;
    double dz = second.aR - first.aR;
    return Math.sqrt(dx * dx + dz * dz);
  }

  private static long milli(double separation) {
    return Math.round(separation * 1000.0D);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
