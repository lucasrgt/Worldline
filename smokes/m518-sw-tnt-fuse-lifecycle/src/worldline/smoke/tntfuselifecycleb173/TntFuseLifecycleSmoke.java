package worldline.smoke.tntfuselifecycleb173;

import java.nio.file.Paths;
import worldline.api.*;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Contrasts the internal primed-TNT fuse with an unprimed block and a mid-fuse stop. */
public final class TntFuseLifecycleSmoke {
  private static final long SEED = 51820240820L;
  private TntFuseLifecycleSmoke() {
  }
  public static void main(String[] a) {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "positive", 81);
    run(trace, "unprimed", 81);
    run(trace, "mid", 40);
    trace.emitTo(System.out);
  }
  private static void run(CanonicalTrace trace, String mode, int ticks) {
    TntWorldBackend backend = new TntWorldBackend(SEED, mode);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      backend.seed();
      backend.snapshot(trace, mode + "-seed");
      for (int tick = 1; tick <= ticks; tick++) {
        runtime.tick();
        if (tick == 1 || tick == 40 || tick == 79 || tick == 80 || tick == 81 || tick == ticks)
          backend.snapshot(trace, mode + "-tick" + tick);
      }
      backend.assertOutcome(ticks);
    } finally {
      runtime.close();
    }
  }
}
