package worldline.smoke.m8;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import worldline.api.WorldSource;
import worldline.b173.B173Mod;
import worldline.b173.B173Observation;
import worldline.b173.B173Runtime;
import worldline.b173.B173Runtimes;
import worldline.mods.LoadedMod;
import worldline.mods.ModLoader;
import worldline.trace.CanonicalStateTrace;

/** Records one identical scenario with no mod or a descriptor-selected version. */
public final class M8VersionSmoke {
  private static final long SEED = 17320110707L;

  private M8VersionSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    require(
        arguments.length == 2 || arguments.length == 3, "expected mode, output, and optional mod");
    if ("baseline".equals(arguments[0]) && arguments.length == 2) {
      record
      (null, Paths.get(arguments[1]), "baseline", "");
    } else if ("mod".equals(arguments[0]) && arguments.length == 3) {
      Path jar = Paths.get(arguments[2]);
      try (LoadedMod<B173Mod> loaded = ModLoader.load(jar, "b1.7.3", "1", B173Mod.class)) {
        record
        (loaded.instance(), Paths.get(arguments[1]), loaded.artifact().descriptor().version(),
            loaded.artifact().sha256());
      }
    } else
      throw new IllegalArgumentException("invalid M8 scenario mode");
  }

  private static void record(B173Mod mod, Path output, String version, String artifact)
      throws Exception {
    B173Runtime runtime = B173Runtimes.create(SEED);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "m8-version-diff")));
      if (mod != null)
        runtime.installMod(mod);
      CanonicalStateTrace trace = new CanonicalStateTrace(SEED, "tick", "block65");
      append(trace, runtime.observe(), "tick0");
      for (int tick = 1; tick <= 3; tick++) {
        runtime.tick();
        append(trace, runtime.observe(), "tick" + tick);
      }
      Files.write(output, trace.value().getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      System.out.println("WORLDLINE_M8_RUN=PASS");
      System.out.println("version=" + version);
      if (!artifact.isEmpty())
        System.out.println("artifact.sha256=" + artifact);
      System.out.println("trace.sha256=" + trace.signature());
    } finally {
      runtime.close();
    }
  }

  private static void append(CanonicalStateTrace trace, B173Observation state, String label) {
    trace.record(label, state.clientTick(), state.blockColumn()[1]);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
