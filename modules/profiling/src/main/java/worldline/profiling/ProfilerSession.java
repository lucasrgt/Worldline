package worldline.profiling;

import java.util.Arrays;

/** One-thread capture session with preallocated frame state and typed metric handles. */
public final class ProfilerSession {
    public interface Sampler {
        void beginFrame();
        void endFrame(ProfilerSession session);
    }

    private final ProfilerRegistry registry;
    private final ProfilerRecorder recorder;
    private final long[] values;
    private final Sampler[] samplers;
    private final ProfilerRegistry.Handle wall;
    private boolean open;
    private boolean sealed;
    private long startedNanos;
    private long ownerThread = -1L;

    public ProfilerSession(ProfilerRegistry registry, int capacity, Sampler... samplers) {
        if (registry == null || samplers == null) throw new NullPointerException("profiler session");
        this.registry = registry;
        this.recorder = new ProfilerRecorder(registry.schema(), capacity);
        this.values = new long[registry.schema().size()];
        this.samplers = samplers.clone();
        for (Sampler sampler : this.samplers)
            if (sampler == null) throw new NullPointerException("profiler sampler");
        this.wall = registry.optional(WorldlineProfilerMetrics.FRAME_WALL);
    }

    public void beginFrame(long sequence, long monotonicNanos) {
        require(!open && !sealed, "profiler session is open or sealed");
        long currentThread = Thread.currentThread().getId();
        if (ownerThread < 0L) ownerThread = currentThread;
        require(ownerThread == currentThread, "profiler session changed thread");
        Arrays.fill(values, 0L);
        recorder.beginFrame(sequence, monotonicNanos);
        startedNanos = monotonicNanos; open = true;
        for (Sampler sampler : samplers) sampler.beginFrame();
    }

    public void set(ProfilerRegistry.Handle metric, long value) {
        requireValue(value); values[index(metric)] = value;
    }

    public void add(ProfilerRegistry.Handle metric, long value) {
        requireValue(value); int index = index(metric);
        values[index] = Math.addExact(values[index], value);
    }

    public void maximum(ProfilerRegistry.Handle metric, long value) {
        requireValue(value); int index = index(metric);
        values[index] = Math.max(values[index], value);
    }

    public long startTimer() {
        require(open, "profiler session frame is not open");
        require(ownerThread == Thread.currentThread().getId(), "profiler session changed thread");
        return System.nanoTime();
    }

    public void addElapsed(ProfilerRegistry.Handle metric, long startNanos, long endNanos) {
        if (metric == null || metric.metric().kind() != ProfilerMetric.Kind.DURATION)
            throw new IllegalArgumentException("elapsed value requires duration metric");
        require(endNanos >= startNanos, "negative profiler elapsed time");
        add(metric, endNanos - startNanos);
    }

    public void endFrame(long monotonicNanos) {
        require(open && monotonicNanos >= startedNanos, "invalid profiler frame end");
        require(ownerThread == Thread.currentThread().getId(), "profiler session changed thread");
        if (wall != null) set(wall, monotonicNanos - startedNanos);
        for (Sampler sampler : samplers) sampler.endFrame(this);
        for (int index = 0; index < values.length; index++) recorder.set(index, values[index]);
        recorder.endFrame(); open = false;
    }

    public FrameCensus snapshot() { return recorder.snapshot(); }
    public ProfilerRun seal(ProfilerRun.Mode mode, long startEpochMillis,
            long endEpochMillis, java.util.Map<String, String> tags) {
        require(!open && !sealed, "profiler session is open or sealed");
        sealed = true;
        return ProfilerRun.of(registry.schema(), recorder.snapshot(), mode,
                startEpochMillis, endEpochMillis, tags);
    }
    public ProfilerRegistry registry() { return registry; }

    private int index(ProfilerRegistry.Handle metric) {
        require(open, "profiler session frame is not open");
        if (metric == null) throw new NullPointerException("profiler metric handle");
        return metric.index(registry);
    }
    private void requireValue(long value) {
        require(open, "profiler session frame is not open");
        require(ownerThread == Thread.currentThread().getId(), "profiler session changed thread");
        require(value >= 0L, "negative profiler metric value");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
