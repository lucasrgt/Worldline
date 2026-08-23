package worldline.smoke.m7;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import worldline.api.WorldSource;
import worldline.b173.B173Mod;
import worldline.b173.B173Observation;
import worldline.b173.B173Runtime;
import worldline.b173.B173Runtimes;
import worldline.mods.LoadedMod;
import worldline.mods.ModArtifact;
import worldline.mods.ModLoader;
import worldline.trace.CanonicalStateTrace;

/** Executes descriptor-selected mod entrypoints against the controlled client. */
public final class M7ModSmoke {
  private static final long SEED = 17320110707L;
  private static final String RUNTIME = "b1.7.3", API = "1";

  private M7ModSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    require(arguments.length >= 2, "expected mode and mod JAR");
    Path jar = Paths.get(arguments[1]);
    if ("run".equals(arguments[0]) && arguments.length == 3)
      run(jar, Integer.parseInt(arguments[2]));
    else if ("reject".equals(arguments[0]) && arguments.length == 2)
      reject(jar);
    else
      throw new IllegalArgumentException("invalid M7 smoke mode");
  }

  private static void run(Path jar, int expectedBlock) throws Exception {
    ModArtifact inspected = ModLoader.inspect(jar, RUNTIME, API);
    require(inspected.compatible(), "compatible benchmark was rejected");
    try (LoadedMod<B173Mod> loaded = ModLoader.load(jar, RUNTIME, API, B173Mod.class)) {
      URL source = loaded.instance().getClass().getProtectionDomain().getCodeSource().getLocation();
      require(source.equals(inspected.path().toUri().toURL()), "entrypoint origin changed");
      B173Runtime runtime = B173Runtimes.create(SEED);
      runtime.bootHeadless();
      try {
        runtime.loadWorld(WorldSource.at(Paths.get("memory", "m7-mod-loading")));
        int base = runtime.observe().blockColumn()[1];
        runtime.installMod(loaded.instance());
        runtime.tick();
        B173Observation modified = runtime.observe();
        require(base == 0 && modified.blockColumn()[1] == expectedBlock,
            "mod did not produce its declared benchmark effect");
        CanonicalStateTrace trace =
            new CanonicalStateTrace(SEED, "tick", "baseBlock65", "modBlock65");
        trace.record("loaded", modified.clientTick(), base, modified.blockColumn()[1]);
        System.out.println("WORLDLINE_M7_MOD=PASS");
        System.out.println("mod.id=" + inspected.descriptor().id());
        System.out.println("mod.entrypoint=" + inspected.descriptor().entrypoint());
        System.out.println("mod.sha256=" + inspected.sha256());
        System.out.println("WORLDLINE_M7_TRACE=" + trace.value());
        System.out.println("WORLDLINE_M7_SIGNATURE=" + trace.signature());
      } finally {
        runtime.close();
      }
    }
  }

  private static void reject(Path jar) throws Exception {
    ModArtifact artifact = ModLoader.inspect(jar, RUNTIME, API);
    try {
      ModLoader.load(jar, RUNTIME, API, B173Mod.class).close();
      throw new IllegalStateException("incompatible mod was loaded");
    } catch (ClassCastException expected) {
      require(artifact.compatible(), "wrong-type package was not metadata-compatible");
      System.out.println("WORLDLINE_M7_REJECT=ENTRYPOINT_TYPE");
    } catch (IllegalStateException expected) {
      require(!artifact.compatible(), "compatible package failed for compatibility");
      System.out.println("WORLDLINE_M7_REJECT=" + artifact.compatibility());
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
