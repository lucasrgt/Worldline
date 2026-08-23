package worldline.smoke.entityitemgroundingb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Contrasts a falling item with an immediately supported control item. */
public final class EntityItemGroundingSmoke {
  private static final long SEED = 50120240820L;
  private static final int TICKS = 30;

  private EntityItemGroundingSmoke() {
  }

  public static void main(String[] arguments) {
    execute().emitTo(System.out);
  }

  private static CanonicalTrace execute() {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "airborne", true);
    run(trace, "supported", false);
    return trace;
  }

  private static void run(CanonicalTrace trace, String label, boolean airborne) {
    ItemWorldBackend backend = new ItemWorldBackend(SEED, airborne);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      backend.seedItem();
      backend.snapshot(trace, label + "-seed");
      for (int tick = 1; tick <= TICKS; tick++) {
        runtime.tick();
        backend.snapshot(trace, label + "-tick" + tick);
      }
      backend.assertOutcome(TICKS);
    } finally {
      runtime.close();
    }
  }
}
