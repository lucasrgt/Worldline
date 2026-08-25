package worldline.smoke.naturalwolfpackb173;

import worldline.api.WorldSource;
import worldline.kernel.GameBackend;

/** Creates one fresh world for each bounded native peaceful-spawn attempt. */
public final class NaturalWolfPackBackend implements GameBackend {
  private final long worldSeed, randomSeed;
  private int attempt, packSize;
  NaturalWolfPackBackend(long worldSeed, long randomSeed) {
    this.worldSeed = worldSeed;
    this.randomSeed = randomSeed;
  }
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }
  public void loadWorld(WorldSource source) {
    attempt = 0;
    packSize = 0;
  }
  public void tick() {
    attempt++;
    String name = "wolf-pack-" + attempt;
    WolfPackWorld world =
        new WolfPackWorld(new WolfMemorySaveHandler(worldSeed, name), name, worldSeed);
    world.prepare();
    world.spawn(randomSeed + attempt);
    packSize = world.coherentPackSize();
  }
  int packSize() {
    return packSize;
  }
  public void close() {
    packSize = 0;
  }
}
