package worldline.smoke.pigwanderb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Contrasts seeded passive pig movement in open and tightly caged terrain. */
public final class PigWanderSmoke {
  private static final long SEED = 50320240820L;
  private static final int TICKS = 240;
  private PigWanderSmoke() {
  }

  public static void main(String[] arguments) {
    execute().emitTo(System.out);
  }

  private static CanonicalTrace execute() {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "open", false);
    run(trace, "caged", true);
    return trace;
  }

  private static void run(CanonicalTrace trace, String label, boolean caged) {
    PigWorldBackend backend = new PigWorldBackend(SEED, caged);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      backend.seedPig();
      backend.snapshot(trace, label + "-seed");
      for (int tick = 1; tick <= TICKS; tick++) {
        runtime.tick();
        if (tick % 5 == 0)
          backend.snapshot(trace, label + "-tick" + tick);
      }
      backend.assertOutcome(TICKS);
    } finally {
      runtime.close();
    }
  }
}
