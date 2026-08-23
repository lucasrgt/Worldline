import worldline.trace.CanonicalTrace;

/** Executes isolated downward water flow against the official server JAR. */
public final class WorldlineWaterDownwardFlowOracle {
  private static final long SEED = 51320240820L;
  private static final int TICKS = 60;
  private static final int X = 8;
  private static final int Z = 8;
  private static final int SOURCE_Y = 68;
  private WorldlineWaterDownwardFlowOracle() {
  }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "open", OracleMemoryChunkLoader.OPEN);
    run(trace, "blocked", OracleMemoryChunkLoader.BLOCKED);
    run(trace, "shaft", OracleMemoryChunkLoader.SHAFT);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, int fixture) {
    dj world = new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke", fixture),
        "worldline-smoke", SEED, null);
    world.r.setSeed(SEED);
    preload(world);
    require(world.b(X, SOURCE_Y, Z, na.B.bn, 0), "water source placement failed");
    snapshot(trace, label + "-seed", world);
    for (int tick = 1; tick <= TICKS; tick++) {
      world.h();
      snapshot(trace, label + "-tick" + tick, world);
    }
    require(water(world.a(X, 68, Z)), "source water disappeared");
    if (fixture == OracleMemoryChunkLoader.BLOCKED) {
      require(world.a(X, 67, Z) == na.u.bn && world.a(X, 66, Z) == 0,
          "blocked column admitted downward water");
    } else if (fixture == OracleMemoryChunkLoader.OPEN) {
      require(water(world.a(X, 67, Z)) && water(world.a(X, 65, Z)) && world.a(X, 64, Z) == na.u.bn,
          "open column did not flow to its floor");
    } else
      require(water(world.a(X, 64, Z)) && water(world.a(X, 61, Z)),
          "shaft did not carry water below the removed floor");
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

  private static boolean water(int id) {
    return id == na.B.bn || id == na.C.bn;
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
