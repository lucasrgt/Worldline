import java.util.ArrayList;
import java.util.TreeSet;
import worldline.api.BlockState;
import worldline.testkit.FlowingFluidObservation;

/** Official-name executor for one moving-fluid block. */
final class OracleFlowingFluidNativeEngine {
  private static final int SOURCE_X = 4;
  private static final int Y = 65;
  private final int moving;
  private final int still;
  private final int step;
  private final int delay;
  private final int gateZ;
  private final OracleFlowingFluidMemorySaveHandler saves;
  private dj world;

  OracleFlowingFluidNativeEngine(long seed, int moving, int still, int step, int delay,
      int gateZ) {
    this.moving = moving;
    this.still = still;
    this.step = step;
    this.delay = delay;
    this.gateZ = gateZ;
    saves = new OracleFlowingFluidMemorySaveHandler(seed, "flowing-fluid-" + moving);
  }

  FlowingFluidObservation execute(long seed) {
    world = open(seed);
    TreeSet<Integer> domain = prepareCascades();
    prepareGate();
    for (int tick = 1; tick <= 240; tick++) {
      world.h();
      scan(domain);
    }
    BlockState blocked = state(SOURCE_X, gateZ);
    require(blocked.equals(new BlockState(still, 0)), "official blocked fluid did not settle");
    require(world.e(SOURCE_X + 1, Y, gateZ, 0), "official fluid gate did not open");
    BlockState recomputed = state(SOURCE_X, gateZ);
    int first = awaitTarget();
    BlockState saved = state(SOURCE_X + 1, gateZ);
    drainLight();
    boolean passable = na.m[moving].e(world, SOURCE_X + 1, Y, gateZ) == null;
    int opacity = na.q[moving];
    int emission = na.s[moving];
    int blockLight = world.a(co.b, SOURCE_X + 1, Y, gateZ);
    int skyLight = world.a(co.a, SOURCE_X + 1, Y, gateZ);
    world.a(true, null);
    world = open(seed);
    BlockState reloaded = state(SOURCE_X + 1, gateZ);
    return new FlowingFluidObservation(moving, new ArrayList<Integer>(domain), first,
        blocked, recomputed, passable, opacity, emission, blockLight, skyLight,
        saved, reloaded);
  }

  private dj open(long seed) {
    dj result = new dj(saves, "flowing-fluid-" + moving, seed, null);
    for (int chunkX = -2; chunkX <= 2; chunkX++)
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        result.c(chunkX, chunkZ);
    return result;
  }

  private TreeSet<Integer> prepareCascades() {
    TreeSet<Integer> domain = new TreeSet<Integer>();
    for (int metadata = 0; metadata <= 7; metadata += step) {
      int distance = metadata / step;
      int z = moving == 8 ? 3 + distance * 3 : -3 - distance * 3;
      trench(z, SOURCE_X + distance);
      require(world.a(SOURCE_X, Y, z, moving, 0),
          "official non-notifying moving-fluid seed failed");
      require(state(SOURCE_X, z).equals(new BlockState(moving, 0)),
          "official moving-fluid seed changed before observation");
      domain.add(0);
      world.c(SOURCE_X, Y, z, moving, delay);
    }
    return domain;
  }

  private void prepareGate() {
    trench(gateZ, -1);
    require(world.a(SOURCE_X, Y, gateZ, still, 0), "official gate source failed");
    require(world.a(SOURCE_X + 1, Y, gateZ, na.u.bn, 0), "official gate failed");
  }

  private void trench(int z, int holeX) {
    for (int x = SOURCE_X - 1; x <= SOURCE_X + 9; x++) {
      world.a(x, Y, z, 0, 0);
      world.a(x, Y, z - 1, na.u.bn, 0);
      world.a(x, Y, z + 1, na.u.bn, 0);
      world.a(x, Y - 1, z, na.u.bn, 0);
    }
    world.a(SOURCE_X - 1, Y, z, na.u.bn, 0);
    world.a(SOURCE_X + 9, Y, z, na.u.bn, 0);
    if (holeX >= SOURCE_X)
      world.a(holeX, Y - 1, z, 0, 0);
  }

  private void scan(TreeSet<Integer> domain) {
    int lanes = moving == 8 ? 8 : 4;
    for (int lane = 0; lane < lanes; lane++) {
      int z = moving == 8 ? 3 + lane * 3 : -3 - lane * 3;
      for (int x = SOURCE_X; x <= SOURCE_X + 8; x++) {
        collect(domain, x, Y, z);
        collect(domain, x, Y - 1, z);
      }
    }
  }

  private void collect(TreeSet<Integer> domain, int x, int y, int z) {
    if (world.a(x, y, z) == moving)
      domain.add(world.c(x, y, z));
  }

  private int awaitTarget() {
    for (int tick = 1; tick <= delay + 2; tick++) {
      world.h();
      if (world.a(SOURCE_X + 1, Y, gateZ) == moving)
        return tick;
    }
    throw new IllegalStateException("official moving-fluid target did not enter scheduler");
  }

  private void drainLight() {
    int passes = 0;
    while (world.f())
      require(++passes <= 128, "official fluid lighting queue did not drain");
  }

  private BlockState state(int x, int z) {
    return new BlockState(world.a(x, Y, z), world.c(x, Y, z));
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
