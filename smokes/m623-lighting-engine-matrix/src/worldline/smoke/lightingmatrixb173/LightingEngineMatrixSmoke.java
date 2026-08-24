package worldline.smoke.lightingmatrixb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Drives one generated-and-updated lighting matrix through the controlled runtime. */
public final class LightingEngineMatrixSmoke {
  private static final long SEED = 62320260823L;

  private LightingEngineMatrixSmoke() {
  }

  public static void main(String[] arguments) {
    LightingEngineMatrixBackend backend = new LightingEngineMatrixBackend(SEED);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    CanonicalTrace trace = new CanonicalTrace(SEED);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "lighting-matrix")));
      runtime.tick();
      backend.record(trace);
      trace.emitTo(System.out);
    } finally {
      runtime.close();
    }
  }
}
