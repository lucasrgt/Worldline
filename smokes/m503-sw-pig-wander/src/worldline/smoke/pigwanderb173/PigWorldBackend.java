package worldline.smoke.pigwanderb173;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Drives one seeded pig through open and tightly caged passive-AI fixtures. */
final class PigWorldBackend implements GameBackend {
  private static final int X = 8;
  private static final int Z = 8;
  private static final double START_X = 8.5D;
  private static final double START_Y = 65.0D;
  private static final double START_Z = 8.5D;
  private final long seed;
  private final boolean caged;
  private World world;
  private SeededPig pig;
  private long maximumHorizontal;
  private long minimumY = Long.MAX_VALUE;
  private long maximumY = Long.MIN_VALUE;

  PigWorldBackend(long seed, boolean caged) {
    this.seed = seed;
    this.caged = caged;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, name, caged), name, seed, null);
    world.rand.setSeed(seed);
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
    observe();
    require(!pig.isDead && current.loadedEntityList.size() == 1, "pig left the fixture");
  }

  @Override
  public void close() {
    world = null;
    pig = null;
  }

  void seedPig() {
    World current = requireWorld();
    pig = new SeededPig(current);
    pig.seedBehavior(seed);
    pig.setPosition(START_X, START_Y, START_Z);
    require(current.entityJoinedWorld(pig), "fixture pig was rejected");
    observe();
  }

  void snapshot(CanonicalTrace trace, String label) {
    World current = requireWorld();
    trace.record(label, current.getWorldTime(), current.loadedEntityList.size(),
        (int) milli(pig.posX), (int) milli(pig.posY), (int) milli(pig.posZ), pig.ticksExisted,
        pig.isDead ? 1 : 0);
  }

  void assertOutcome(int ticks) {
    require(pig.ticksExisted == ticks, "pig tick age drifted");
    require(minimumY >= 64_900L && maximumY <= 66_100L, "pig escaped vertically");
    if (caged)
      require(maximumHorizontal <= 250L, "caged pig escaped: " + maximumHorizontal);
    else
      require(maximumHorizontal >= 500L && maximumHorizontal <= 12_000L,
          "open pig did not wander within bounds: " + maximumHorizontal);
  }

  private void observe() {
    long dx = milli(pig.posX - START_X);
    long dz = milli(pig.posZ - START_Z);
    maximumHorizontal = Math.max(maximumHorizontal, Math.round(Math.sqrt(dx * dx + dz * dz)));
    minimumY = Math.min(minimumY, milli(pig.posY));
    maximumY = Math.max(maximumY, milli(pig.posY));
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
