package worldline.smoke.redstone;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Places one torch-and-dust fixture and ticks the public runtime. */
public final class RedstoneWireSmoke {
  private static final long SEED = 17320110707L;

  private RedstoneWireSmoke() {
  }

  public static void main(String[] arguments) {
    executeScenario().emitTo(System.out);
  }

  private static CanonicalTrace executeScenario() {
    RedstoneWorldBackend backend = new RedstoneWorldBackend(SEED);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      CanonicalTrace trace = new CanonicalTrace(SEED);
      backend.snapshot(trace, "initial");
      backend.placeCircuit();
      backend.snapshot(trace, "placed");
      for (int tick = 1; tick <= 4; tick++) {
        runtime.tick();
        backend.snapshot(trace, "tick" + tick);
      }
      backend.assertFinalState();
      return trace;
    } finally {
      runtime.close();
    }
  }
}
