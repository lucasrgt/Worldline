package worldline.stationapi.runtime;

import worldline.profiling.ProfilerMetric;
import worldline.profiling.ProfilerRegistry;

/** Public StationAPI bridge for opt-in mod-owned profiler metrics. */
public final class StationApiProfiler {
    private StationApiProfiler() {}

    public static Metric register(ProfilerMetric metric) {
        return StationApiProfilerRuntime.register(metric);
    }

    public static boolean active() { return StationApiProfilerRuntime.frameOpen(); }

    public static long startTimer() { return active() ? System.nanoTime() : 0L; }

    public static void add(Metric metric, long value) {
        if (metric == null) throw new NullPointerException("profiler metric");
        StationApiProfilerRuntime.add(metric, value);
    }

    public static void maximum(Metric metric, long value) {
        if (metric == null) throw new NullPointerException("profiler metric");
        StationApiProfilerRuntime.maximum(metric, value);
    }

    public static void addElapsed(Metric metric, long startNanos) {
        if (startNanos != 0L) add(metric, System.nanoTime() - startNanos);
    }

    public static final class Metric {
        final ProfilerMetric definition;
        ProfilerRegistry.Handle handle;
        Metric(ProfilerMetric definition) { this.definition = definition; }
        public String name() { return definition.name(); }
    }
}
