import java.util.ArrayList;
import java.util.List;
import worldline.trace.CanonicalTrace;

/** Executes the terrain pathfinding matrix directly against the official server JAR. */
public final class WorldlinePathfindingMatrixOracle {
  private static final long SEED = 62220260823L;

  private WorldlinePathfindingMatrixOracle() {
  }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "open", 0);
    run(trace, "detour", 1);
    run(trace, "sealed", 2);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, int terrain) {
    dj world = new dj(new OracleMemorySaveHandler(SEED, label, terrain), label, SEED, null);
    for (int chunkX = -2; chunkX <= 2; chunkX++)
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.c(chunkX, chunkZ);
    oc pig = new oc(world);
    pig.c(4.5D, 65.0D, 8.5D);
    require(world.b(pig), "fixture pig was rejected");
    cb path = world.a(pig, 12, 65, 8, 32.0F);
    require(path != null, "pathfinder returned no bounded fallback");
    List<int[]> nodes = new ArrayList<>();
    while (!path.b()) {
      ba point = path.a(pig);
      nodes.add(new int[] {milli(point.a), milli(point.b), milli(point.c)});
      path.a();
      require(nodes.size() <= 64, "path exceeded fixture bound");
    }
    verify(nodes, terrain);
    int[] last = nodes.get(nodes.size() - 1);
    trace.record(label + "-summary", 0L, 1, terrain, nodes.size(), last[0], last[1], last[2]);
    for (int index = 0; index < nodes.size(); index++) {
      int[] node = nodes.get(index);
      trace.record(label + "-node" + index, 0L, 1, node[0], node[1], node[2]);
    }
  }

  private static void verify(List<int[]> nodes, int terrain) {
    require(!nodes.isEmpty(), "pathfinder produced an empty route");
    int[] last = nodes.get(nodes.size() - 1);
    int maximumZ = Integer.MIN_VALUE;
    for (int[] node : nodes)
      maximumZ = Math.max(maximumZ, node[2]);
    if (terrain == 0)
      require(last[0] >= 12_000 && maximumZ <= 9_000, "open route drifted");
    else if (terrain == 1)
      require(last[0] >= 12_000 && maximumZ >= 12_000, "wall detour omitted its gap");
    else
      require(last[0] <= 10_500, "sealed route entered the target ring");
  }

  private static int milli(double value) {
    return (int) Math.round(value * 1000.0D);
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
