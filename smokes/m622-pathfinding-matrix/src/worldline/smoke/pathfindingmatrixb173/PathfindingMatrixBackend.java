package worldline.smoke.pathfindingmatrixb173;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.src.EntityPig;
import net.minecraft.src.PathEntity;
import net.minecraft.src.Vec3D;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Requests and records exact vanilla path nodes for one terrain fixture. */
public final class PathfindingMatrixBackend implements GameBackend {
  private static final int TARGET_X = 12;
  private static final int TARGET_Y = 65;
  private static final int TARGET_Z = 8;
  private final long seed;
  private final int terrain;
  private final List<int[]> nodes = new ArrayList<>();
  private World world;
  private EntityPig pig;

  PathfindingMatrixBackend(long seed, int terrain) {
    this.seed = seed;
    this.terrain = terrain;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, name, terrain), name, seed, null);
    for (int chunkX = -2; chunkX <= 2; chunkX++)
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.getChunkFromChunkCoords(chunkX, chunkZ);
    pig = new EntityPig(world);
    pig.setPosition(4.5D, 65.0D, 8.5D);
    require(world.entityJoinedWorld(pig), "fixture pig was rejected");
  }

  @Override
  public void tick() {
    require(nodes.isEmpty(), "route requested more than once");
    PathEntity path = world.getEntityPathToXYZ(pig, TARGET_X, TARGET_Y, TARGET_Z, 32.0F);
    require(path != null, "pathfinder returned no bounded fallback");
    while (!path.isFinished()) {
      Vec3D point = path.getPosition(pig);
      nodes.add(new int[] {milli(point.xCoord), milli(point.yCoord), milli(point.zCoord)});
      path.incrementPathIndex();
      require(nodes.size() <= 64, "path exceeded fixture bound");
    }
    verifyRoute();
  }

  void record(CanonicalTrace trace, String label) {
    int[] last = nodes.get(nodes.size() - 1);
    trace.record(label + "-summary", 0L, 1, terrain, nodes.size(), last[0], last[1], last[2]);
    for (int index = 0; index < nodes.size(); index++) {
      int[] node = nodes.get(index);
      trace.record(label + "-node" + index, 0L, 1, node[0], node[1], node[2]);
    }
  }

  @Override
  public void close() {
    world = null;
    pig = null;
  }

  private void verifyRoute() {
    require(!nodes.isEmpty(), "pathfinder produced an empty route");
    int[] last = nodes.get(nodes.size() - 1);
    if (terrain == 0) {
      require(last[0] >= 12_000 && maximumZ() <= 9_000, "open route drifted");
    } else if (terrain == 1) {
      require(last[0] >= 12_000 && maximumZ() >= 12_000, "wall detour omitted its gap");
    } else {
      require(last[0] <= 10_500, "sealed route entered the target ring");
    }
  }

  private int maximumZ() {
    int maximum = Integer.MIN_VALUE;
    for (int[] node : nodes)
      maximum = Math.max(maximum, node[2]);
    return maximum;
  }

  private static int milli(double value) {
    return (int) Math.round(value * 1000.0D);
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
