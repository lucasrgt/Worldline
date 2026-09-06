package worldline.m794;

import java.util.Arrays;
import worldline.profiling.*;

/** Shared profiler definition validated before starting an official client. */
public final class ProductionMetricSchema {
  static final String[] NAMES = {"render", "animated", "pages", "allocation", "matches", "bytes", "animatedallocation",
      "pageallocation", "rebuilds", "resolved", "reused", "transformed", "vertexreuse", "immediate", "posetime",
      "transformtime", "packtime", "uniquetime", "drivertime", "threadcpu", "gc", "jit", "gpudrain", "renderwithdrain"};
  static final ProfilerSchema SCHEMA = ProfilerSchema.of(Arrays.asList(duration("render"), duration("animated"),
      duration("pages"), bytes("allocation"), WorldlineProfilerMetrics.extensionCounter("mod.aero.matches", "aero"),
      bytes("bytes"), bytes("animatedallocation"), bytes("pageallocation"), counter("rebuilds"), counter("resolved"),
      counter("reused"), counter("transformed"), counter("vertexreuse"), counter("immediate"), duration("posetime"),
      duration("transformtime"), duration("packtime"), duration("uniquetime"), duration("drivertime"),
      duration("threadcpu"), duration("gc"), duration("jit"), duration("gpudrain"), duration("renderwithdrain")));

  private ProductionMetricSchema() {}

  public static void main(String[] args) {
    for (String name : NAMES)
      if (!SCHEMA.contains("mod.aero." + name))
        throw new IllegalStateException("profile channel missing: " + name);
    System.out.println("WORLDLINE_M794_PROFILE_SCHEMA=PASS");
  }

  private static ProfilerMetric counter(String name) {
    return WorldlineProfilerMetrics.extensionCounter("mod.aero." + name, "aero");
  }
  private static ProfilerMetric duration(String name) {
    return WorldlineProfilerMetrics.extensionDuration("mod.aero." + name, "aero");
  }
  private static ProfilerMetric bytes(String name) {
    return ProfilerMetric.of("mod.aero." + name, "aero", ProfilerMetric.Unit.BYTES, ProfilerMetric.Kind.GAUGE,
        ProfilerMetric.Causality.DIAGNOSTIC);
  }
}
