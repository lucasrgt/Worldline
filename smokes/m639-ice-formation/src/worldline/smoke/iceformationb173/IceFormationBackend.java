package worldline.smoke.iceformationb173;

import net.minecraft.src.Block;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;

/** Advances native ambient scheduling in either the dark fixture or its lit control. */
public final class IceFormationBackend implements GameBackend {
  private final long seed;
  private final boolean lit;
  private IceWorld world;
  IceFormationBackend(long seed, boolean lit) {
    this.seed = seed;
    this.lit = lit;
  }
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new IceWorld(new IceMemorySaveHandler(seed, name, lit), name, seed);
    world.rand.setSeed(seed);
    world.prepare();
    require(world.observation()[0] == Block.waterStill.blockID,
        "ice fixture did not begin as still water");
  }
  public void tick() {
    world.ambientPass();
  }
  int[] observation() {
    return world.observation();
  }
  int iceCount() {
    return world.iceCount();
  }
  public void close() {
    world = null;
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
