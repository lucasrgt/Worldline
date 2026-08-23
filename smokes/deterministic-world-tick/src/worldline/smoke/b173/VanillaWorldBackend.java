package worldline.smoke.b173;

import net.minecraft.src.Block;
import net.minecraft.src.BlockSand;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Bridges the product runtime port to the mapped vanilla world used by this smoke. */
final class VanillaWorldBackend implements GameBackend {
  private static final int X = 8;
  private static final int Z = 8;
  private static final int SAND_Y = 70;

  private final long seed;
  private World world;

  VanillaWorldBackend(long seed) {
    this.seed = seed;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
    BlockSand.fallInstantly = true;
  }

  @Override
  public void loadWorld(WorldSource source) {
    String worldName = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, worldName), worldName, seed, null);
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
        world.getChunkFromChunkCoords(chunkX, chunkZ);
      }
    }
    require(world.getBlockId(X, 64, Z) == Block.stone.blockID, "fixture stone missing");
    require(world.getBlockId(X, SAND_Y, Z) == 0, "drop cell is not air");
  }

  @Override
  public void tick() {
    requireWorld().tick();
  }

  @Override
  public void close() {
    world = null;
  }

  void placeSand() {
    require(requireWorld().setBlockWithNotify(X, SAND_Y, Z, Block.sand.blockID),
        "sand placement failed");
  }

  void snapshot(CanonicalTrace trace, String label) {
    World current = requireWorld();
    int[] column = new int[SAND_Y - 63];
    for (int y = 64; y <= SAND_Y; y++) {
      column[y - 64] = current.getBlockId(X, y, Z);
    }
    trace.record(label, current.getWorldTime(), current.loadedEntityList.size(), column);
  }

  void assertFinalState() {
    World current = requireWorld();
    require(current.getBlockId(X, SAND_Y, Z) == 0, "sand remained in drop cell");
    require(current.getBlockId(X, 65, Z) == Block.sand.blockID, "sand did not land on stone");
    require(current.getBlockId(X, 64, Z) == Block.stone.blockID, "fixture stone changed");
  }

  private World requireWorld() {
    if (world == null) {
      throw new IllegalStateException("vanilla world is not loaded");
    }
    return world;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
