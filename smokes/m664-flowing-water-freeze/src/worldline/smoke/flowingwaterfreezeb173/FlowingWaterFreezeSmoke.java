package worldline.smoke.flowingwaterfreezeb173;

import java.nio.file.Paths;
import net.minecraft.src.Block;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Runs the native cold-biome freeze boundary with flowing water as its control. */
public final class FlowingWaterFreezeSmoke {
  private static final long SEED = 1772835215L;
  private static final int MAXIMUM_PASSES = 16;
  private FlowingWaterFreezeSmoke() {
  }

  public static void main(String[] arguments) {
    FlowingWaterFreezeBackend backend = new FlowingWaterFreezeBackend(SEED);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "flowing-water-freeze")));
      int[] state = backend.observation();
      require(state[0] == Block.waterStill.blockID && state[1] == 0,
          "freeze fixture did not begin with still water");
      require(state[2] == Block.waterMoving.blockID
          && state[3] == FlowingWaterFreezeBackend.FLOWING_METADATA,
          "freeze fixture did not begin with flowing water control");
      int pass = 0;
      while (state[0] != Block.ice.blockID && pass < MAXIMUM_PASSES) {
        pass++;
        runtime.tick();
        state = backend.observation();
        require(state[4] == 1 && state[5] < 10 && state[6] < 10,
            "freeze cells left the cold low-light boundary");
        require(state[2] == Block.waterMoving.blockID
            && state[3] == FlowingWaterFreezeBackend.FLOWING_METADATA,
            "flowing water froze or changed level");
        require(state[0] == Block.waterStill.blockID || state[0] == Block.ice.blockID,
            "still water changed unexpectedly");
      }
      require(state[0] == Block.ice.blockID && state[1] == 0,
          "still water did not freeze after bounded ambient passes");
      CanonicalTrace trace = new CanonicalTrace(SEED);
      trace.record("still-freeze-flowing-stays", 0L, 0, Block.waterStill.blockID,
          Block.ice.blockID, Block.waterMoving.blockID,
          FlowingWaterFreezeBackend.FLOWING_METADATA, state[4], state[5], state[6],
          MAXIMUM_PASSES);
      trace.emitTo(System.out);
    } finally {
      runtime.close();
    }
  }

  private static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
