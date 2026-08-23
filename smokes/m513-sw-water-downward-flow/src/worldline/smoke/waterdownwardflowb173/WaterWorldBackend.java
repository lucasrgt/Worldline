package worldline.smoke.waterdownwardflowb173;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Drives scheduled downward water flow through isolated column fixtures. */
final class WaterWorldBackend implements GameBackend {
  private static final int X = 8;
  private static final int Z = 8;
  private static final int SOURCE_Y = 68;
  private final long seed;
  private final int fixture;
  private World world;

  WaterWorldBackend(long seed, int fixture) {
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
    world = new World(new MemorySaveHandler(seed, name, fixture), name, seed, null);
    world.rand.setSeed(seed);
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.getChunkFromChunkCoords(chunkX, chunkZ);
    }
    require(world.setBlockAndMetadataWithNotify(X, SOURCE_Y, Z, Block.waterMoving.blockID, 0),
        "water source placement failed");
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
    int[] values = new int[16];
    for (int offset = 0; offset < 8; offset++) {
      int y = SOURCE_Y - offset;
      values[offset * 2] = current.getBlockId(X, y, Z);
      values[1 + offset * 2] = current.getBlockMetadata(X, y, Z);
    }
    trace.record(label, current.getWorldTime(), 0, values);
  }

  void assertOutcome() {
    require(water(id(68)), "source water disappeared");
    if (fixture == MemoryChunkLoader.BLOCKED) {
      require(
          id(67) == Block.stone.blockID && id(66) == 0, "blocked column admitted downward water");
    } else if (fixture == MemoryChunkLoader.OPEN) {
      require(water(id(67)) && water(id(65)) && id(64) == Block.stone.blockID,
          "open column did not flow to its floor");
    } else
      require(water(id(64)) && water(id(61)), "shaft did not carry water below the removed floor");
  }

  private int id(int y) {
    return requireWorld().getBlockId(X, y, Z);
  }
  private static boolean water(int id) {
    return id == Block.waterMoving.blockID || id == Block.waterStill.blockID;
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
