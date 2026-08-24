package worldline.smoke.lightningfireb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Contrasts native lightning ignition on normal and easy difficulty. */
public final class LightningFireSmoke {
  private static final long SEED = 58920260824L;
  private static final int TICKS = 2;

  private LightningFireSmoke() { }

  public static void main(String[] arguments) {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "normal", 2);
    run(trace, "easy", 1);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, int difficulty) {
    LightningFireBackend backend = new LightningFireBackend(SEED, difficulty);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", label)));
      backend.snapshot(trace, label + "-seed");
      backend.strike();
      backend.snapshot(trace, label + "-strike");
      for (int tick = 1; tick <= TICKS; tick++) {
        runtime.tick();
        backend.snapshot(trace, label + "-tick" + tick);
      }
      backend.assertOutcome();
    } finally {
      runtime.close();
    }
  }
}
