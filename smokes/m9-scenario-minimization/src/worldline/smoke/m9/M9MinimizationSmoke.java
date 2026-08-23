package worldline.smoke.m9;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import worldline.analysis.TraceDiff;
import worldline.api.WorldSource;
import worldline.b173.B173Mod;
import worldline.b173.B173Observation;
import worldline.b173.B173Runtime;
import worldline.b173.B173Runtimes;
import worldline.minimization.DivergenceFingerprint;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioMinimizer;
import worldline.invariants.InvariantFields;
import worldline.semantics.SemanticSteps;
import worldline.mods.LoadedMod;
import worldline.mods.ModLoader;
import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateTrace;

/** Reexecutes two mod versions while minimizing an exact divergence predicate. */
public final class M9MinimizationSmoke {
  private static final long SEED = 17320110707L;
  private static final List<String> STEPS = Arrays.asList("observe:before", "reseed:101", "tap:2",
      "tap:6", "tick", "reseed:202", "observe:target", "tick", "observe:after");

  private M9MinimizationSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    require(arguments.length == 4, "expected v1, v2, original, and minimized paths");
    Path v1 = Paths.get(arguments[0]), v2 = Paths.get(arguments[1]);
    Scenario original = Scenario.of(STEPS);
    DivergenceFingerprint target = DivergenceFingerprint.from(compare(original, v1, v2));
    ScenarioMinimizer.Result result = ScenarioMinimizer.minimize(original, 100,
        candidate -> preserves(candidate, v1, v2, target), SemanticSteps::disposable);
    require(result.complete()
            && result.minimized().steps().equals(
                Arrays.asList("observe:before", "tick", "observe:target")),
        "unexpected minimized scenario: " + result.minimized().steps());
    require(target.matches(compare(result.minimized(), v1, v2)), "minimized predicate changed");
    for (int index = 0; index < result.minimized().size(); index++) {
      List<String> reduced = new ArrayList<>(result.minimized().steps());
      reduced.remove(index);
      require(
          !target.matches(compare(Scenario.of(reduced), v1, v2)), "scenario is not one-minimal");
    }
    Files.write(Paths.get(arguments[2]), original.bytes(), StandardOpenOption.CREATE_NEW);
    Files.write(Paths.get(arguments[3]), result.minimized().bytes(), StandardOpenOption.CREATE_NEW);
    String report = "original.steps=" + original.size()
        + "\nminimized.steps=" + result.minimized().size()
        + "\nremoved.steps=" + result.removedSteps() + "\nevaluations=" + result.evaluations()
        + "\ncomplete=" + result.complete() + "\noriginal.sha256=" + original.sha256()
        + "\nminimized.sha256=" + result.minimized().sha256()
        + "\nv1.artifact.sha256=" + ModLoader.inspect(v1, "b1.7.3", "1").sha256()
        + "\nv2.artifact.sha256=" + ModLoader.inspect(v2, "b1.7.3", "1").sha256()
        + "\nfingerprint.sha256=" + sha256(target.render())
        + "\nsteps=observe:before,tick,observe:target\n"
        + "invariant=" + InvariantFields.rule("block65") + "\n";
    System.out.println("WORLDLINE_M9_MINIMIZATION=PASS");
    System.out.print(report);
    System.out.println("evidence.sha256=" + sha256(report));
  }

  private static boolean preserves(
      Scenario scenario, Path left, Path right, DivergenceFingerprint target) {
    try {
      return target.matches(compare(scenario, left, right));
    } catch (Exception error) {
      throw new IllegalStateException("candidate execution failed", error);
    }
  }

  private static TraceDiff compare(Scenario scenario, Path left, Path right) throws Exception {
    return TraceDiff.compare(run(scenario, left), run(scenario, right));
  }

  private static CanonicalStateDocument run(Scenario scenario, Path jar) throws Exception {
    try (LoadedMod<B173Mod> loaded = ModLoader.load(jar, "b1.7.3", "1", B173Mod.class)) {
      B173Runtime runtime = B173Runtimes.create(SEED);
      runtime.bootHeadless();
      try {
        runtime.loadWorld(WorldSource.at(Paths.get("memory", "m9-minimization")));
        runtime.installMod(loaded.instance());
        CanonicalStateTrace trace = new CanonicalStateTrace(SEED, "tick", "block65");
        for (String step : scenario.steps())
          apply(step, runtime, trace);
        return CanonicalStateDocument.parse(trace.value());
      } finally {
        runtime.close();
      }
    }
  }

  private static void apply(String step, B173Runtime runtime, CanonicalStateTrace trace) {
    if (step.equals("tick"))
      runtime.tick();
    else if (step.startsWith("reseed:"))
      runtime.reseed(Long.parseLong(step.substring(7)));
    else if (step.startsWith("tap:"))
      runtime.tap(Integer.parseInt(step.substring(4)));
    else if (step.startsWith("observe:")) {
      B173Observation state = runtime.observe();
      trace.record(step.substring(8), state.clientTick(), state.blockColumn()[1]);
    } else
      throw new IllegalArgumentException("unknown M9 step: " + step);
  }

  private static String sha256(String text) throws Exception {
    byte[] digest =
        MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte item : digest)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
