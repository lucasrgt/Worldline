package worldline.smoke.snowaccumulationb173;

import java.nio.file.Paths;
import worldline.api.*;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Runs paired snowfall and dry native ambient schedulers. */
public final class SnowAccumulationSmoke {
  private static final long SEED = 1772835215L;
  private static final int MAXIMUM_PASSES = 16;
  private SnowAccumulationSmoke() {}
  public static void main(String[] arguments) {
    SnowAccumulationBackend wet = new SnowAccumulationBackend(SEED, true);
    SnowAccumulationBackend dry = new SnowAccumulationBackend(SEED, false);
    MinecraftRuntime wetRuntime = new ControlledMinecraftRuntime(wet);
    MinecraftRuntime dryRuntime = new ControlledMinecraftRuntime(dry);
    wetRuntime.bootHeadless();
    dryRuntime.bootHeadless();
    try {
      wetRuntime.loadWorld(WorldSource.at(Paths.get("memory", "snowfall")));
      dryRuntime.loadWorld(WorldSource.at(Paths.get("memory", "dry")));
      int[] wetState = wet.observation(), dryState = dry.observation();
      int pass = 0;
      while (wetState[0] != 78 && pass < MAXIMUM_PASSES) {
        pass++;
        wetRuntime.tick();
        dryRuntime.tick();
        wetState = wet.observation();
        dryState = dry.observation();
        require(wetState[2] == 1 && dryState[2] == 1, "snow cell left cold biome");
        require(dryState[3] == 0 && dryState[0] == 0, "dry control changed");
        require(wetState[0] == 0 || wetState[0] == 78, "snowfall cell changed unexpectedly");
      }
      require(wetState[0] == 78 && wetState[3] == 1 && wetState[4] < 10,
          "snow layer absent after bounded snowfall passes");
      CanonicalTrace trace = new CanonicalTrace(SEED);
      trace.record("accumulated", 0L, 0, 0, 78, wetState[4], MAXIMUM_PASSES, 1, 0);
      trace.emitTo(System.out);
    } finally {
      wetRuntime.close();
      dryRuntime.close();
    }
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
