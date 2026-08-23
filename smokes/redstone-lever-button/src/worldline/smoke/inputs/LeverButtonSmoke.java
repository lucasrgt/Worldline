package worldline.smoke.inputs;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Places a powered lever and a pressed button, then waits for the button pulse. */
public final class LeverButtonSmoke {
  private static final long SEED = 17320110707L;

  private LeverButtonSmoke() {
  }

  public static void main(String[] arguments) {
    LeverButtonBackend backend = new LeverButtonBackend(SEED);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      CanonicalTrace trace = new CanonicalTrace(SEED);
      backend.snapshot(trace, "initial");
      backend.placeCircuit();
      backend.snapshot(trace, "placed");
      backend.assertPlacedState();
      for (int tick = 1; tick <= 22; tick++) {
        runtime.tick();
        backend.snapshot(trace, "tick" + tick);
      }
      backend.assertFinalState();
      trace.emitTo(System.out);
    } finally {
      runtime.close();
    }
  }
}
