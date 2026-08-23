import worldline.trace.CanonicalTrace;

/** Executes the item-grounding fixture directly against the official server JAR. */
public final class WorldlineEntityItemGroundingOracle {
  private static final long SEED = 50120240820L;
  private static final int X = 8;
  private static final int Z = 8;
  private static final int TICKS = 30;
  private static final double GROUND_Y = 65.125D;
  private static final double AIR_Y = 68.125D;

  private WorldlineEntityItemGroundingOracle() {
  }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "airborne", true);
    run(trace, "supported", false);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, boolean airborne) {
    dj world =
        new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke"), "worldline-smoke", SEED, null);
    preload(world);
    require(world.a(X, 64, Z) == na.u.bn, "fixture stone missing");
    ez item = new ez(world);
    item.c(X, airborne ? AIR_Y : GROUND_Y, Z);
    item.aS = 0.0D;
    item.aT = 0.0D;
    item.aU = 0.0D;
    require(world.b(item), "fixture item was rejected");
    long initialY = milli(item.aQ);
    long minimumY = initialY;
    boolean sawGround = false;
    snapshot(trace, label + "-seed", world, item);
    for (int tick = 1; tick <= TICKS; tick++) {
      world.h();
      world.e();
      minimumY = Math.min(minimumY, milli(item.aQ));
      sawGround |= item.ba;
      snapshot(trace, label + "-tick" + tick, world, item);
    }
    long finalY = milli(item.aQ);
    require(item.b == TICKS && sawGround,
        "item did not complete the grounding/age path: age=" + item.b + " ground=" + sawGround
            + " y=" + finalY + " motionY=" + milli(item.aT));
    require(finalY >= 65_000L && finalY <= 65_250L, "item escaped the stone surface: " + finalY);
    require(milli(item.aT) == 0L, "grounded item retained vertical motion");
    if (airborne)
      require(initialY - minimumY >= 2_500L, "airborne item did not descend");
    else
      require(Math.abs(finalY - initialY) <= 125L, "supported item drifted vertically");
  }

  private static void preload(dj world) {
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.c(chunkX, chunkZ);
    }
  }

  private static void snapshot(CanonicalTrace trace, String label, dj world, ez item) {
    trace.record(label, world.m(), world.b.size(), (int) milli(item.aQ), (int) milli(item.aT),
        item.ba ? 1 : 0, item.b);
  }

  private static long milli(double value) {
    return Math.round(value * 1000.0D);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
