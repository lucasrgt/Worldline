package worldline.smoke.m17;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.aero.AeroChunkReadiness;
import worldline.aero.AeroDiagnostics;

/** Aggregates one bounded M17 policy/scenario window plus its drain state. */
final class RunMetrics {
  final int frames, allFrames;
  long calls, falseReturns, trueReturns, forced, accepted, proposed;
  long visibleAccepted, budgetStops, deferred, stalled, budgetSkipped;
  long finalQueue, finalDirty, finalVisible, finalVisibleDirty, finalVisibleReady;
  long frameMaxUs, frameP95Us, compileMaxUs, maxOldest;
  double bestReadyRatio;

  private RunMetrics(int frames, int allFrames) {
    this.frames = frames;
    this.allFrames = allFrames;
  }

  static RunMetrics load(Path build, String scenario, String policy, int limit) throws Exception {
    String prefix = scenario + "-" + policy;
    List<AeroChunkReadiness.Sample> probes = new ArrayList<>();
    for (String row :
        Files.readAllLines(build.resolve(prefix + "-probe.log"), StandardCharsets.UTF_8))
      if (row.startsWith("[WorldlineChunkProbe]"))
        probes.add(AeroChunkReadiness.parse(row));
    require(probes.size() >= limit, "too few readiness frames for " + prefix);
    RunMetrics value = new RunMetrics(limit, probes.size());
    for (AeroChunkReadiness.Sample sample : probes.subList(0, limit))
      value.add(sample);
    AeroChunkReadiness.Sample last = probes.get(probes.size() - 1);
    value.finalQueue = last.queue;
    value.finalDirty = last.dirty;
    value.finalVisible = last.visible;
    value.finalVisibleDirty = last.visibleDirty;
    value.finalVisibleReady = last.visibleReady;
    value.addTiming(build.resolve(prefix + "-aero.log"), limit);
    return value;
  }

  private void add(AeroChunkReadiness.Sample sample) {
    calls += sample.calls;
    falseReturns += sample.falseReturns;
    trueReturns += sample.trueReturns;
    forced += sample.forced;
    accepted += sample.accepted;
    proposed += sample.proposed;
    visibleAccepted += sample.visibleAccepted;
    budgetStops += sample.budgetStops;
    deferred += sample.deferred;
    stalled += sample.stalled;
    maxOldest = Math.max(maxOldest, sample.oldest);
    compileMaxUs = Math.max(compileMaxUs, sample.compileUs);
    if (sample.visible > 0)
      bestReadyRatio = Math.max(bestReadyRatio, (double) sample.visibleReady / sample.visible);
  }

  private void addTiming(Path path, int limit) throws Exception {
    List<Long> frameTimes = new ArrayList<>();
    for (String row : Files.readAllLines(path, StandardCharsets.UTF_8)) {
      if (!row.startsWith("[Aero_"))
        continue;
      AeroDiagnostics.Sample sample = AeroDiagnostics.parse(row);
      if (frameTimes.size() < limit) {
        frameTimes.add(sample.frameUs);
        compileMaxUs = Math.max(compileMaxUs, sample.compileMaxUs);
        budgetSkipped += sample.budgetSkipped;
      }
    }
    require(frameTimes.size() >= limit, "too few timing frames in " + path);
    Collections.sort(frameTimes);
    frameMaxUs = frameTimes.get(limit - 1);
    frameP95Us = frameTimes.get((int) ((limit - 1) * 0.95D));
  }

  boolean globallyDrained() {
    return finalQueue == 0 && finalDirty == 0 && finalVisibleDirty == 0
        && finalVisibleReady == finalVisible;
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
