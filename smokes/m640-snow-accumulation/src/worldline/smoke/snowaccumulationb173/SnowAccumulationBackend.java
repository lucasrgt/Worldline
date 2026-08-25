package worldline.smoke.snowaccumulationb173;

import worldline.api.WorldSource;
import worldline.kernel.GameBackend;

/** Advances the native scheduler in either a snowfall fixture or its dry control. */
public final class SnowAccumulationBackend implements GameBackend {
  private final long seed;
  private final boolean snowfall;
  private SnowWorld world;
  SnowAccumulationBackend(long seed, boolean snowfall) {
    this.seed = seed;
    this.snowfall = snowfall;
  }
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new SnowWorld(new SnowMemorySaveHandler(seed, name, snowfall), name, seed, snowfall);
    world.prepare();
    world.rand.setSeed(seed);
    require(world.observation()[0] == 0, "snow fixture did not begin as air");
  }
  public void tick() {
    world.ambientPass();
  }
  int[] observation() {
    return world.observation();
  }
  public void close() {
    world = null;
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
