package worldline.smoke.piston;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Torch-powered piston facing east, with tile-entity stepping. */
final class PistonExtendBackend implements GameBackend {
  private static final int PISTON_X = 8;
  private static final int TORCH_X = 7;
  private static final int HEAD_X = 9;
  private static final int Y = 65;
  private static final int Z = 8;

  private final long seed;
  private World world;

  PistonExtendBackend(long seed) {
    this.seed = seed;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
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
    require(world.getBlockId(PISTON_X, 64, Z) == Block.stone.blockID, "stone missing");
  }

  @Override
  public void tick() {
    World current = requireWorld();
    current.updateEntities();
    current.tick();
  }

  @Override
  public void close() {
    world = null;
  }

  void placeCircuit() {
    World current = requireWorld();
    require(current.setBlockAndMetadataWithNotify(PISTON_X, Y, Z, Block.pistonBase.blockID, 5),
        "piston placement failed");
    require(
        current.setBlockAndMetadataWithNotify(TORCH_X, Y, Z, Block.torchRedstoneActive.blockID, 5),
        "torch placement failed");
  }

  void snapshot(CanonicalTrace trace, String label) {
    World current = requireWorld();
    trace.record(label, current.getWorldTime(), current.loadedEntityList.size(),
        current.getBlockId(PISTON_X, Y, Z), current.getBlockMetadata(PISTON_X, Y, Z),
        current.getBlockId(HEAD_X, Y, Z));
  }

  void assertFinalState() {
    World current = requireWorld();
    require((current.getBlockMetadata(PISTON_X, Y, Z) & 8) != 0, "piston did not extend");
    require(current.getBlockId(HEAD_X, Y, Z) == Block.pistonExtension.blockID
            || current.getBlockId(HEAD_X, Y, Z) == Block.pistonMoving.blockID,
        "piston head missing");
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
