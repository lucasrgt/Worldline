package worldline.smoke.entitydynamics;
import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityBoat;
import net.minecraft.src.EntityGhast;
import net.minecraft.src.EntityMinecart;
import net.minecraft.src.EntitySlime;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;
/** Runs four isolated vanilla entity-motion fixtures. */
final class EntityDynamicsBackend implements GameBackend {
  private final long seed;
  private final String mode;
  private World world;
  private Entity entity;
  private long minY, maxY;
  private boolean sawGround, sawAir, sawCollision;
  EntityDynamicsBackend(long s, String m) {
    seed = s;
    mode = m;
  }
  public void bootHeadless() {
  }
  public void loadWorld(WorldSource source) {
    String n = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, n), n, seed, null);
    world.difficultySetting = 2;
    for (int x = -3; x <= 3; x++)
      for (int z = -3; z <= 3; z++)
        world.getChunkFromChunkCoords(x, z);
    setup();
  }
  public void tick() {
    entity.onUpdate();
    sample();
  }
  public void close() {
    world = null;
    entity = null;
  }
  private void setup() {
    if (mode.startsWith("ghast"))
      ghast();
    else if (mode.startsWith("slime"))
      slime();
    else if (mode.startsWith("boat"))
      boat();
    else
      cart();
    minY = maxY = milli(entity.posY);
    sample();
  }
  private void ghast() {
    EntityGhast g = new EntityGhast(world);
    g.setPosition(8D, 80D, 8D);
    g.waypointX = 8D;
    g.waypointY = 88D;
    g.waypointZ = 8D;
    g.courseChangeCooldown = 0;
    seed(g);
    entity = g;
    if (mode.endsWith("roof"))
      fill(5, 11, 83, 83, 5, 11, Block.stone.blockID);
  }
  private void slime() {
    EntitySlime s = new EntitySlime(world);
    s.setSlimeSize(1);
    s.setPosition(8D, 65D, 8D);
    s.rotationYaw = 0F;
    seed(s);
    field(s, "ticksTillJump", 0);
    entity = s;
    if (mode.endsWith("roof"))
      fill(7, 9, 66, 66, 7, 9, Block.stone.blockID);
  }
  private void boat() {
    EntityBoat b = new EntityBoat(world, 9.2D, 65D, 8.5D);
    b.motionX = 0.1D;
    seed(b);
    entity = b;
    if (mode.endsWith("wall"))
      fill(10, 10, 65, 67, 7, 9, Block.stone.blockID);
  }
  private void cart() {
    int end = mode.endsWith("short") ? 9 : 30;
    for (int x = 8; x <= end; x++)
      world.setBlockAndMetadataWithNotify(x, 65, 8, Block.minecartTrack.blockID, 1);
    EntityMinecart c = new EntityMinecart(world, 8.5D, 65D, 8.5D, 0);
    c.motionX = 0.3D;
    seed(c);
    entity = c;
  }
  void snapshot(CanonicalTrace t, String l) {
    t.record(l, 0, entity.isDead ? 0 : 1, (int) milli(entity.posX), (int) milli(entity.posY),
        (int) milli(entity.posZ), (int) milli(entity.motionX), (int) milli(entity.motionY),
        entity.onGround ? 1 : 0, entity.isCollidedHorizontally ? 1 : 0);
  }
  void assertOutcome() {
    if (mode.equals("ghast-open"))
      req(maxY - minY > 200, "ghast did not drift");
    if (mode.equals("ghast-roof"))
      req(maxY - minY < 200, "roof did not constrain ghast");
    if (mode.equals("slime-open"))
      req(sawAir && sawGround && maxY - minY > 100, "slime jump/landing absent");
    if (mode.equals("slime-roof"))
      req(maxY - minY < 700, "roof did not bound slime: " + (maxY - minY));
    if (mode.equals("boat-wall"))
      req(sawCollision, "boat missed wall");
    if (mode.equals("boat-open"))
      req(!sawCollision && entity.posX > 9.3D, "open boat stalled");
    if (mode.equals("cart-short"))
      req(Math.abs(entity.motionX) < 0.05D, "off-rail cart did not brake");
    if (mode.equals("cart-long"))
      req(Math.abs(entity.motionX) > 0.05D, "rail control stopped");
  }
  private void sample() {
    minY = Math.min(minY, milli(entity.posY));
    maxY = Math.max(maxY, milli(entity.posY));
    sawGround |= entity.onGround;
    sawAir |= !entity.onGround;
    sawCollision |= entity.isCollidedHorizontally;
  }
  private void fill(int xa, int xb, int ya, int yb, int za, int zb, int id) {
    for (int x = xa; x <= xb; x++)
      for (int y = ya; y <= yb; y++)
        for (int z = za; z <= zb; z++)
          world.setBlockWithNotify(x, y, z, id);
  }
  private static long milli(double v) {
    return Math.round(v * 1000D);
  }
  private static void seed(Entity e) {
    try {
      Field f = Entity.class.getDeclaredField("rand");
      f.setAccessible(true);
      ((Random) f.get(e)).setSeed(50450820240821L);
    } catch (ReflectiveOperationException x) {
      throw new IllegalStateException(x);
    }
  }
  private static void field(Object o, String n, int v) {
    try {
      Field f = o.getClass().getDeclaredField(n);
      f.setAccessible(true);
      f.setInt(o, v);
    } catch (ReflectiveOperationException x) {
      throw new IllegalStateException(x);
    }
  }
  private static void req(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
