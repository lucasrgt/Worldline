package worldline.smoke.lightningfireb173;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Drives the deterministic center-cell portion of native lightning ignition. */
final class LightningFireBackend implements GameBackend {
  private static final int X = 8;
  private static final int Y = 65;
  private static final int Z = 8;
  private final long seed;
  private final int difficulty;
  private World world;
  private SeededLightning lightning;

  LightningFireBackend(long seed, int difficulty) {
    this.seed = seed;
    this.difficulty = difficulty;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new World(new LightningMemorySaveHandler(seed, name), name, seed, null);
    world.difficultySetting = difficulty;
    world.rand.setSeed(seed);
    for (int chunkX = -2; chunkX <= 2; chunkX++)
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.getChunkFromChunkCoords(chunkX, chunkZ);
    require(world.getBlockId(X, Y - 1, Z) == Block.stone.blockID, "stone support absent");
    require(world.getBlockId(X, Y, Z) == 0, "strike cell was not air");
  }

  void strike() {
    lightning = new SeededLightning(requireWorld(), X + 0.5D, Y, Z + 0.5D, seed);
    require(world.entityJoinedWorld(lightning), "lightning entity was rejected");
  }

  @Override
  public void tick() {
    requireWorld().tick();
    world.updateEntities();
  }

  void snapshot(CanonicalTrace trace, String label) {
    World current = requireWorld();
    trace.record(label, current.getWorldTime(), current.loadedEntityList.size(), difficulty,
        current.getBlockId(X, Y, Z), current.getBlockMetadata(X, Y, Z),
        lightning == null ? -1 : lightning.ticksExisted,
        lightning != null && lightning.isDead ? 1 : 0);
  }

  void assertOutcome() {
    int expected = difficulty >= 2 ? Block.fire.blockID : 0;
    require(requireWorld().getBlockId(X, Y, Z) == expected,
        "lightning center ignition drifted at difficulty " + difficulty);
    require(lightning != null && lightning.ticksExisted == 2 && !lightning.isDead,
        "lightning two-tick lifecycle drifted");
  }

  @Override
  public void close() {
    world = null;
    lightning = null;
  }

  private World requireWorld() {
    if (world == null) throw new IllegalStateException("lightning world is not loaded");
    return world;
  }
  private static void require(boolean condition, String message) {
    if (!condition) throw new IllegalStateException(message);
  }
}
