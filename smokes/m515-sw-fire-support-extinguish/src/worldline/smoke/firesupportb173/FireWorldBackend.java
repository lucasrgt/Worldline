package worldline.smoke.firesupportb173;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Drives fire support changes through vanilla neighbor notifications. */
final class FireWorldBackend implements GameBackend {
  static final int LOST = 0;
  static final int RETAINED = 1;
  static final int UNSUPPORTED = 2;
  private static final int X = 8;
  private static final int Z = 8;
  private static final int SUPPORT_Y = 64;
  private final long seed;
  private final int fixture;
  private World world;

  FireWorldBackend(long seed, int fixture) {
    this.seed = seed;
    this.fixture = fixture;
  }
  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new World(new FireMemorySaveHandler(seed, name), name, seed, null);
    world.rand.setSeed(seed);
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.getChunkFromChunkCoords(chunkX, chunkZ);
    }
    if (fixture == UNSUPPORTED)
      removeSupport();
    require(world.setBlockAndMetadataWithNotify(X, SUPPORT_Y + 1, Z, Block.fire.blockID, 0),
        "fire placement did not change the cell");
    if (fixture == UNSUPPORTED)
      require(fireId() == 0, "unsupported fire survived placement");
    else
      require(fireId() == Block.fire.blockID, "supported fire was not placed");
  }

  void trigger() {
    if (fixture == LOST)
      removeSupport();
  }
  @Override
  public void tick() {
    requireWorld().tick();
  }
  @Override
  public void close() {
    world = null;
  }

  void snapshot(CanonicalTrace trace, String label) {
    World current = requireWorld();
    trace.record(label, current.getWorldTime(), 0,
        new int[] {current.getBlockId(X, SUPPORT_Y, Z), current.getBlockMetadata(X, SUPPORT_Y, Z),
            current.getBlockId(X, SUPPORT_Y + 1, Z),
            current.getBlockMetadata(X, SUPPORT_Y + 1, Z)});
  }

  void assertOutcome() {
    if (fixture == RETAINED)
      require(supportId() == Block.stone.blockID && fireId() == Block.fire.blockID,
          "supported fire did not remain");
    else
      require(supportId() == 0 && fireId() == 0, "unsupported fire or support remained");
  }

  private void removeSupport() {
    if (requireWorld().getBlockId(X, SUPPORT_Y, Z) == 0)
      return;
    require(world.setBlockWithNotify(X, SUPPORT_Y, Z, 0), "support removal failed");
  }
  private int supportId() {
    return requireWorld().getBlockId(X, SUPPORT_Y, Z);
  }
  private int fireId() {
    return requireWorld().getBlockId(X, SUPPORT_Y + 1, Z);
  }
  private World requireWorld() {
    if (world == null)
      throw new IllegalStateException("vanilla world is not loaded");
    return world;
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
