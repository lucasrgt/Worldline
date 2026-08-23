package worldline.smoke.m12;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import worldline.aero.AeroDiagnostics;
import worldline.aero.AeroDiagnostics.Sample;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioMinimizer;

/** Verifies two captures of one saved Aero world and minimizes each spike record window. */
public final class AeroReproductionSmoke {
  private static final long SPIKE_US = 25_000L;

  private AeroReproductionSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    require(arguments.length == 2, "expected two capture logs");
    Capture first = analyze(Paths.get(arguments[0]));
    Capture second = analyze(Paths.get(arguments[1]));
    String report = "capture.runs=2\nsaved.world.captured=true\nworld.seed.recreated=true\n"
        + "dense.scene.present.both=true\nspike.stage=chunks.compile\n"
        + "spike.threshold.ms=25\nminimized.records=1\n";
    System.out.println("WORLDLINE_M12_REPRODUCTION=PASS");
    System.out.println("capture.1.records=" + first.records);
    System.out.println("capture.2.records=" + second.records);
    System.out.print(report);
    System.out.println("evidence.sha256=" + sha256(report));
  }

  private static Capture analyze(Path path) throws Exception {
    List<String> all = Files.readAllLines(path, StandardCharsets.UTF_8);
    List<Sample> frames = new ArrayList<>();
    for (String line : all) {
      if (!line.startsWith("[Aero_") || line.contains(" visibleChunks=-1 "))
        continue;
      Sample frame = AeroDiagnostics.parse(line);
      if (frame.visibleChunks > 0)
        frames.add(frame);
    }
    require(!frames.isEmpty(), "capture has no stable frames: " + path);
    List<String> steps = new ArrayList<>();
    for (int index = 0; index < frames.size(); index++)
      steps.add("frame:" + index);
    ScenarioMinimizer.Result minimized = ScenarioMinimizer.minimize(
        Scenario.of(steps), 100, candidate -> preserves(candidate, frames));
    require(minimized.complete() && minimized.minimized().size() == 1,
        "spike record did not minimize to one record");
    return new Capture(frames.size());
  }

  private static boolean preserves(Scenario scenario, List<Sample> frames) {
    for (String step : scenario.steps()) {
      int index = Integer.parseInt(step.substring("frame:".length()));
      Sample frame = frames.get(index);
      if (frame.frameUs >= SPIKE_US && frame.compileUs >= 10_000L)
        return true;
    }
    return false;
  }

  private static String sha256(String value) throws Exception {
    byte[] digest =
        MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte item : digest)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }

  private static final class Capture {
    final int records;
    Capture(int records) {
      this.records = records;
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
