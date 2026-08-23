import worldline.trace.CanonicalTrace;

/** Executes isolated downward lava flow against the official server JAR. */
public final class WorldlineLavaDownwardFlowOracle {
  private static final long SEED = 51420240820L;
  private static final int TICKS = 240;
  private static final int X = 8;
  private static final int Z = 8;
  private static final int SOURCE_Y = 68;
  private WorldlineLavaDownwardFlowOracle() {
  }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "open", OracleLavaMemoryChunkLoader.OPEN);
    run(trace, "blocked", OracleLavaMemoryChunkLoader.BLOCKED);
    run(trace, "shaft", OracleLavaMemoryChunkLoader.SHAFT);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, int fixture) {
    dj world = new dj(new OracleLavaMemorySaveHandler(SEED, "worldline-smoke", fixture),
        "worldline-smoke", SEED, null);
    world.r.setSeed(SEED);
    preload(world);
    require(world.b(X, SOURCE_Y, Z, na.D.bn, 0), "lava source placement failed");
    snapshot(trace, label + "-seed", world);
    for (int tick = 1; tick <= TICKS; tick++) {
      world.h();
      snapshot(trace, label + "-tick" + tick, world);
    }
    require(lava(world.a(X, 68, Z)), "source lava disappeared");
    if (fixture == OracleLavaMemoryChunkLoader.BLOCKED) {
      require(world.a(X, 67, Z) == na.u.bn && world.a(X, 66, Z) == 0,
          "blocked column admitted downward lava");
    } else if (fixture == OracleLavaMemoryChunkLoader.OPEN) {
      require(lava(world.a(X, 67, Z)) && lava(world.a(X, 65, Z)) && world.a(X, 64, Z) == na.u.bn,
          "open column did not flow to its floor");
    } else
      require(lava(world.a(X, 64, Z)) && lava(world.a(X, 61, Z)),
          "shaft did not carry lava below the removed floor");
  }

  private static void preload(dj world) {
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.c(chunkX, chunkZ);
    }
  }

  private static void snapshot(CanonicalTrace trace, String label, dj world) {
    int[] values = new int[16];
    for (int offset = 0; offset < 8; offset++) {
      int y = SOURCE_Y - offset;
      values[offset * 2] = world.a(X, y, Z);
      values[1 + offset * 2] = world.c(X, y, Z);
    }
    trace.record(label, world.m(), 0, values);
  }

  private static boolean lava(int id) {
    return id == na.D.bn || id == na.E.bn;
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
