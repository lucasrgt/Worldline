package worldline.profiling;

/** Loader-neutral bridge for opt-in mod-owned client profiler metrics. */
public final class ClientProfiler {
    private ClientProfiler() {}

    public static Metric register(ProfilerMetric metric) {
        return ClientProfilerRuntime.register(metric);
    }

    public static boolean active() { return ClientProfilerRuntime.frameOpen(); }

    public static long startTimer() { return active() ? System.nanoTime() : 0L; }

    public static void add(Metric metric, long value) {
        if (metric == null) throw new NullPointerException("profiler metric");
        ClientProfilerRuntime.add(metric, value);
    }

    public static void maximum(Metric metric, long value) {
        if (metric == null) throw new NullPointerException("profiler metric");
        ClientProfilerRuntime.maximum(metric, value);
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
