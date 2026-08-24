package worldline.smoke.pathfindingmatrixb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Contrasts direct route construction across three deterministic terrain families. */
public final class PathfindingMatrixSmoke {
  private static final long SEED = 62220260823L;

  private PathfindingMatrixSmoke() {
  }

  public static void main(String[] arguments) {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    run(trace, "open", 0);
    run(trace, "detour", 1);
    run(trace, "sealed", 2);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, String label, int terrain) {
    PathfindingMatrixBackend backend = new PathfindingMatrixBackend(SEED, terrain);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", label)));
      runtime.tick();
      backend.record(trace, label);
    } finally {
      runtime.close();
    }
  }
}
