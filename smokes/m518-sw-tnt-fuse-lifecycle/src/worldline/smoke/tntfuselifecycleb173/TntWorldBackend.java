package worldline.smoke.tntfuselifecycleb173;

import net.minecraft.src.*;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Drives one mapped EntityTNTPrimed through the vanilla entity-update boundary. */
final class TntWorldBackend implements GameBackend {
  private static final int X = 8, Y = 65, Z = 8;
  private final long seed;
  private final String mode;
  private World world;
  private EntityTNTPrimed tnt;
  TntWorldBackend(long seed, String mode) {
    this.seed = seed;
    this.mode = mode;
  }
  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }
  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, name), name, seed, null);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        world.getChunkFromChunkCoords(x, z);
    require(world.getBlockId(X, Y - 1, Z) == Block.stone.blockID, "fixture stone missing");
  }
  @Override
  public void tick() {
    World current = requireWorld();
    current.tick();
    current.updateEntities();
  }
  @Override
  public void close() {
    world = null;
    tnt = null;
  }
  void seed() {
    World current = requireWorld();
    if (mode.equals("unprimed")) {
      require(
          current.setBlockWithNotify(X, Y, Z, Block.tnt.blockID), "unprimed TNT placement failed");
      return;
    }
    tnt = new EntityTNTPrimed(current, X + 0.5D, Y, Z + 0.5D);
    tnt.motionX = 0D;
    tnt.motionY = 0D;
    tnt.motionZ = 0D;
    require(tnt.fuse == 80 && current.entityJoinedWorld(tnt), "primed TNT seed failed");
  }
  void snapshot(CanonicalTrace trace, String label) {
    World current = requireWorld();
    boolean present = tnt != null && current.loadedEntityList.contains(tnt);
    trace.record(label, current.getWorldTime(), present ? 1 : 0, tnt == null ? -999 : tnt.fuse,
        tnt != null && tnt.isDead ? 1 : 0, current.getBlockId(X, Y, Z));
  }
  void assertOutcome(int ticks) {
    World current = requireWorld();
    if (mode.equals("unprimed")) {
      require(
          tnt == null && current.getBlockId(X, Y, Z) == Block.tnt.blockID, "unprimed TNT changed");
      return;
    }
    if (mode.equals("mid")) {
      require(
          ticks == 40 && tnt.fuse == 40 && !tnt.isDead && current.loadedEntityList.contains(tnt),
          "mid-fuse state drift");
      return;
    }
    require(ticks == 81 && tnt.fuse == -1 && tnt.isDead && !current.loadedEntityList.contains(tnt),
        "terminal fuse state drift: fuse=" + tnt.fuse + " dead=" + tnt.isDead);
  }
  private World requireWorld() {
    if (world == null)
      throw new IllegalStateException("vanilla world not loaded");
    return world;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
