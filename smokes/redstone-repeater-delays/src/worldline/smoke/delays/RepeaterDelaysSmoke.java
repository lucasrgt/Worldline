package worldline.smoke.delays;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Places delay-2/3/4 repeaters and ticks until each lock-on. */
public final class RepeaterDelaysSmoke {
  private static final long SEED = 17320110707L;

  private RepeaterDelaysSmoke() {
  }

  public static void main(String[] arguments) {
    RepeaterDelaysBackend backend = new RepeaterDelaysBackend(SEED);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      CanonicalTrace trace = new CanonicalTrace(SEED);
      backend.snapshot(trace, "initial");
      backend.placeCircuit();
      backend.snapshot(trace, "placed");
      backend.assertPlacedState();
      for (int tick = 1; tick <= 8; tick++) {
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
