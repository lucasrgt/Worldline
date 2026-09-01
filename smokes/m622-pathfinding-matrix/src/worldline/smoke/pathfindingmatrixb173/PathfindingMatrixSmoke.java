package worldline.smoke.pathfindingmatrixb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.PathfindingMatrixEvidence;
import worldline.testkit.PathfindingMatrixFixture;
import worldline.testkit.PathfindingMatrixObservation;
import worldline.testkit.PathfindingRouteObservation;
import worldline.trace.CanonicalTrace;

/** Contrasts direct route construction across three deterministic terrain families. */
public final class PathfindingMatrixSmoke {
  private static final long SEED = 62220260823L;

  private PathfindingMatrixSmoke() {
  }

  public static void main(String[] arguments) {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    PathfindingRouteObservation open = run(trace, "open", 0);
    PathfindingRouteObservation detour = run(trace, "detour", 1);
    PathfindingRouteObservation sealed = run(trace, "sealed", 2);
    PathfindingMatrixEvidence evidence = PathfindingMatrixFixture.execute(
        () -> new PathfindingMatrixObservation(open, detour, sealed));
    if (evidence.canonical().isEmpty())
      throw new IllegalStateException("pathfinding evidence was empty");
    trace.emitTo(System.out);
  }

  private static PathfindingRouteObservation run(CanonicalTrace trace, String label,
      int terrain) {
    PathfindingMatrixBackend backend = new PathfindingMatrixBackend(SEED, terrain);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", label)));
      runtime.tick();
      backend.record(trace, label);
      return backend.observation(label);
    } finally {
      runtime.close();
    }
  }
}
