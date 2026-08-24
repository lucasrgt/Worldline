package worldline.smoke.weathermatrixb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Runs five countdown transitions through the controlled runtime. */
public final class WeatherStateMatrixSmoke {
  private static final long SEED = 62420260823L;

  private WeatherStateMatrixSmoke() { }

  public static void main(String[] arguments) {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    for (int weatherCase = 0; weatherCase < 5; weatherCase++)
      run(trace, weatherCase);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, int weatherCase) {
    WeatherStateMatrixBackend backend = new WeatherStateMatrixBackend(SEED, weatherCase);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "weather-" + weatherCase)));
      runtime.tick();
      backend.record(trace);
    } finally {
      runtime.close();
    }
  }
}
