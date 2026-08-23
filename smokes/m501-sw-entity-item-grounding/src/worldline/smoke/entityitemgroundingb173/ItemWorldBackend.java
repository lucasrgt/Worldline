package worldline.smoke.entityitemgroundingb173;

import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Drives one neutral item through vanilla gravity and ground collision. */
final class ItemWorldBackend implements GameBackend {
  private static final int X = 8;
  private static final int Z = 8;
  private static final double GROUND_Y = 65.125D;
  private static final double AIR_Y = 68.125D;

  private final long seed;
  private final boolean airborne;
  private World world;
  private EntityItem item;
  private long initialY;
  private long minimumY;
  private boolean sawGround;

  ItemWorldBackend(long seed, boolean airborne) {
    this.seed = seed;
    this.airborne = airborne;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, name), name, seed, null);
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.getChunkFromChunkCoords(chunkX, chunkZ);
    }
    require(world.getBlockId(X, 64, Z) == Block.stone.blockID, "fixture stone missing");
  }

  @Override
  public void tick() {
    World current = requireWorld();
    current.tick();
    current.updateEntities();
    minimumY = Math.min(minimumY, milli(item.posY));
    sawGround |= item.onGround;
  }

  @Override
  public void close() {
    world = null;
    item = null;
  }

  void seedItem() {
    World current = requireWorld();
    item = new EntityItem(current);
    item.setPosition(X, airborne ? AIR_Y : GROUND_Y, Z);
    item.motionX = 0.0D;
    item.motionY = 0.0D;
    item.motionZ = 0.0D;
    require(current.entityJoinedWorld(item), "fixture item was rejected");
    initialY = milli(item.posY);
    minimumY = initialY;
  }

  void snapshot(CanonicalTrace trace, String label) {
    World current = requireWorld();
    trace.record(label, current.getWorldTime(), current.loadedEntityList.size(),
        (int) milli(item.posY), (int) milli(item.motionY), item.onGround ? 1 : 0, item.age);
  }

  void assertOutcome(int ticks) {
    long finalY = milli(item.posY);
    require(item.age == ticks && sawGround,
        "item did not complete the grounding/age path: age=" + item.age + " ground=" + sawGround
            + " y=" + finalY + " motionY=" + milli(item.motionY));
    require(finalY >= 65_000L && finalY <= 65_250L, "item escaped the stone surface: " + finalY);
    require(milli(item.motionY) == 0L, "grounded item retained vertical motion");
    if (airborne)
      require(initialY - minimumY >= 2_500L, "airborne item did not descend");
    else
      require(Math.abs(finalY - initialY) <= 125L, "supported item drifted vertically");
  }

  private World requireWorld() {
    if (world == null)
      throw new IllegalStateException("vanilla world is not loaded");
    return world;
  }

  private static long milli(double value) {
    return Math.round(value * 1000.0D);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
