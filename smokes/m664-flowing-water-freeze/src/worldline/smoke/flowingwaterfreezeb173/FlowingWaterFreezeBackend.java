package worldline.smoke.flowingwaterfreezeb173;

import net.minecraft.src.Block;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;

/** Owns one mapped native world for the flowing-water freeze boundary. */
public final class FlowingWaterFreezeBackend implements GameBackend {
  static final int FLOWING_METADATA = 1;
  private final long seed;
  private FlowingWaterWorld world;

  FlowingWaterFreezeBackend(long seed) {
    this.seed = seed;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new FlowingWaterWorld(new FlowingWaterMemorySaveHandler(seed, name), name, seed);
    world.rand.setSeed(seed);
    world.prepare();
    int[] state = observation();
    require(state[0] == Block.waterStill.blockID && state[1] == 0,
        "mapped freeze fixture did not begin with still water");
    require(state[2] == Block.waterMoving.blockID && state[3] == FLOWING_METADATA,
        "mapped flowing-water control was not installed");
  }

  @Override
  public void tick() {
    requireWorld().ambientPass();
  }

  int[] observation() {
    return requireWorld().observation();
  }

  @Override
  public void close() {
    world = null;
  }

  private FlowingWaterWorld requireWorld() {
    if (world == null) {
      throw new IllegalStateException("mapped world is not loaded");
    }
    return world;
  }

  private static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
