package worldline.smoke.itemdespawn;
import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;
/** Exercises lifetime expiry, a live control, and collection removal. */
final class ItemDespawnBackend implements GameBackend {
  private final long seed;
  private World world;
  private EntityItem item;
  ItemDespawnBackend(long s) {
    seed = s;
  }
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }
  public void loadWorld(WorldSource s) {
    String n = s.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, n), n, seed, null);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        world.getChunkFromChunkCoords(x, z);
  }
  public void tick() {
    world.tick();
    world.updateEntities();
  }
  public void close() {
    world = null;
    item = null;
  }
  void seed(int age) {
    item = new EntityItem(world, 8D, 65.125D, 8D, new ItemStack(Block.stone));
    item.motionX = 0D;
    item.motionY = 0D;
    item.motionZ = 0D;
    item.age = age;
    require(world.entityJoinedWorld(item), "item rejected");
  }
  void collect() {
    item.onCollideWithPlayer(new EntityPlayer(world) {});
    world.updateEntities();
  }
  void snapshot(CanonicalTrace t, String l) {
    t.record(l, world.getWorldTime(), world.loadedEntityList.size(), item.age, item.isDead ? 1 : 0,
        world.loadedEntityList.contains(item) ? 1 : 0, item.item.stackSize);
  }
  void requireState(int age, boolean dead, boolean present) {
    require(
        item.age == age && item.isDead == dead && world.loadedEntityList.contains(item) == present,
        "item state drift");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
