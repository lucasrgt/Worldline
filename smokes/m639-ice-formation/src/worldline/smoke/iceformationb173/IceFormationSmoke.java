package worldline.smoke.iceformationb173;

import java.nio.file.Paths;
import worldline.api.*;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Runs paired low-light and high-light native ambient schedulers. */
public final class IceFormationSmoke {
  private static final long SEED = 1772835215L;
  private static final int MAXIMUM_PASSES = 16;
  private IceFormationSmoke() {}
  public static void main(String[] arguments) {
    IceFormationBackend dark = new IceFormationBackend(SEED, false);
    IceFormationBackend lit = new IceFormationBackend(SEED, true);
    MinecraftRuntime darkRuntime = new ControlledMinecraftRuntime(dark);
    MinecraftRuntime litRuntime = new ControlledMinecraftRuntime(lit);
    darkRuntime.bootHeadless();
    litRuntime.bootHeadless();
    try {
      darkRuntime.loadWorld(WorldSource.at(Paths.get("memory", "ice-dark")));
      litRuntime.loadWorld(WorldSource.at(Paths.get("memory", "ice-lit")));
      int[] darkState = dark.observation(), litState = lit.observation();
      int pass = 0;
      while (darkState[0] != 79 && pass < MAXIMUM_PASSES) {
        pass++;
        darkRuntime.tick();
        litRuntime.tick();
        darkState = dark.observation();
        litState = lit.observation();
        require(darkState[2] == 1 && litState[2] == 1, "formation cell left cold biome");
        require(litState[3] >= 10 && litState[0] == 9, "lit control water changed");
        require(darkState[0] == 9 || darkState[0] == 79, "dark water changed unexpectedly");
      }
      require(darkState[0] == 79 && darkState[3] < 10, "ice absent after bounded ambient passes");
      CanonicalTrace trace = new CanonicalTrace(SEED);
      trace.record("formed", 0L, 0, 9, 79, darkState[3], litState[3], MAXIMUM_PASSES, 1, 0);
      trace.emitTo(System.out);
    } finally {
      darkRuntime.close();
      litRuntime.close();
    }
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
