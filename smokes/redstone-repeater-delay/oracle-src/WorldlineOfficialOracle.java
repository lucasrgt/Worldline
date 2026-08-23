import worldline.trace.CanonicalTrace;

/** Executes the delay-1 repeater fixture against the official obfuscated server JAR. */
public final class WorldlineOfficialOracle {
  private static final long SEED = 17320110707L;
  private static final int TORCH_X = 8;
  private static final int REPEATER_X = 9;
  private static final int WIRE_X = 10;
  private static final int OBSERVE_X = 11;
  private static final int Y = 65;
  private static final int Z = 8;
  private static final int FACE_EAST = 4;

  private WorldlineOfficialOracle() {
  }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    dj world =
        new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke"), "worldline-smoke", SEED, null);
    preload(world);
    require(world.a(TORCH_X, 64, Z) == na.u.bn, "fixture stone missing");
    require(world.a(TORCH_X, Y, Z) == 0, "torch cell is not air");
    require(world.a(REPEATER_X, Y, Z) == 0, "repeater cell is not air");
    require(world.a(WIRE_X, Y, Z) == 0, "wire cell is not air");

    CanonicalTrace trace = new CanonicalTrace(SEED);
    snapshot(trace, "initial", world);
    require(world.b(TORCH_X, Y, Z, na.aR.bn, 5), "torch placement failed");
    require(world.b(REPEATER_X, Y, Z, na.bi.bn, 1), "repeater placement failed");
    require(world.e(WIRE_X, Y, Z, na.aw.bn), "wire placement failed");
    snapshot(trace, "placed", world);
    require(world.a(REPEATER_X, Y, Z) == na.bi.bn, "repeater locked on during placement");
    require(world.c(WIRE_X, Y, Z) == 0, "wire powered during placement");
    require(poweringTo(world) == 0, "repeater output live during placement");
    for (int tick = 1; tick <= 6; tick++) {
      world.h();
      snapshot(trace, "tick" + tick, world);
    }

    require(world.a(REPEATER_X, Y, Z) == na.bj.bn, "repeater stayed idle");
    require(world.a(WIRE_X, Y, Z) == na.aw.bn, "wire missing");
    require(world.c(WIRE_X, Y, Z) > 0, "wire has no power");
    require(poweringTo(world) == 1, "repeater output is dark");
    require(world.r(OBSERVE_X, Y, Z), "observer is unpowered");
    require(world.a(TORCH_X, 64, Z) == na.u.bn, "fixture stone changed");
    trace.emitTo(System.out);
  }

  private static void preload(dj world) {
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
        world.c(chunkX, chunkZ);
      }
    }
  }

  private static void snapshot(CanonicalTrace trace, String label, dj world) {
    int powered = world.r(OBSERVE_X, Y, Z) ? 1 : 0;
    trace.record(label, world.m(), world.b.size(), world.a(REPEATER_X, Y, Z), world.a(WIRE_X, Y, Z),
        world.c(WIRE_X, Y, Z), poweringTo(world), powered);
  }

  private static int poweringTo(dj world) {
    return world.a(REPEATER_X, Y, Z) == na.bj.bn && na.bj.a((pb) world, REPEATER_X, Y, Z, FACE_EAST)
        ? 1
        : 0;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
