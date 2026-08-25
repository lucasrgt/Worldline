package worldline.smoke.naturalwolfpackb173;

import java.nio.file.Paths;
import worldline.api.*;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Runs bounded fresh-world attempts through the native peaceful spawner. */
public final class NaturalWolfPackSmoke {
  private static final long WORLD_SEED = 1772835215L;
  private static final long RANDOM_SEED = 64120260824L;
  private static final int MAXIMUM_ATTEMPTS = 64;
  private NaturalWolfPackSmoke() {}
  public static void main(String[] arguments) {
    NaturalWolfPackBackend backend = new NaturalWolfPackBackend(WORLD_SEED, RANDOM_SEED);
    MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "natural-wolf-pack")));
      int packSize = 0;
      for (int attempt = 1; attempt <= MAXIMUM_ATTEMPTS && packSize < 2; attempt++) {
        runtime.tick();
        packSize = backend.packSize();
      }
      require(packSize >= 2 && packSize <= 8, "natural wolf pack absent after bounded attempts");
      CanonicalTrace trace = new CanonicalTrace(WORLD_SEED);
      trace.record("pack", 0L, 0, 95, 2, 8, MAXIMUM_ATTEMPTS, 1);
      trace.emitTo(System.out);
    } finally {
      runtime.close();
    }
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
