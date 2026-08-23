package worldline.smoke.firesupportb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Contrasts removed, retained, and initially absent fire support. */
public final class FireSupportSmoke {
  private static final long SEED = 51520240820L;
  private static final int TICKS = 2;
  private FireSupportSmoke() {
  }
  public static void main(String[] arguments) {
    execute().emitTo(System.out);
  }

  private static CanonicalTrace execute() {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "lost", FireWorldBackend.LOST);
    run(trace, "retained", FireWorldBackend.RETAINED);
    run(trace, "unsupported", FireWorldBackend.UNSUPPORTED);
    return trace;
  }

  private static void run(CanonicalTrace trace, String label, int fixture) {
    FireWorldBackend backend = new FireWorldBackend(SEED, fixture);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      backend.snapshot(trace, label + "-seed");
      backend.trigger();
      backend.snapshot(trace, label + "-action");
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
