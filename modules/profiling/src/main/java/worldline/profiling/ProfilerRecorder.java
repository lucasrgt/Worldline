package worldline.profiling;

/** Fixed-capacity, allocation-free hot-path recorder for complete frame rows. */
public final class ProfilerRecorder {
    private final ProfilerSchema schema;
    private final long[][] rows;
    private final int[] observedEpoch;
    private int frames, epoch = 1;
    private boolean open;

    public ProfilerRecorder(ProfilerSchema schema, int capacity) {
        if (schema == null) throw new NullPointerException("profiler schema");
        require(capacity > 0 && capacity <= 5_000_000, "profiler capacity");
        this.schema = schema;
        this.rows = new long[capacity][schema.size() + 2];
        this.observedEpoch = new int[schema.size()];
    }

    public void beginFrame(long sequence, long monotonicNanos) {
        require(!open, "profiler frame already open");
        require(frames < rows.length, "profiler capacity exceeded");
        require(sequence >= 0L && monotonicNanos >= 0L, "negative profiler frame identity");
        if (frames > 0) {
            require(rows[frames - 1][0] < Long.MAX_VALUE
                    && sequence == rows[frames - 1][0] + 1L, "noncontiguous profiler frame");
            require(monotonicNanos > rows[frames - 1][1], "nonmonotonic profiler frame");
        }
        if (epoch == Integer.MAX_VALUE) {
            java.util.Arrays.fill(observedEpoch, 0); epoch = 1;
        }
        rows[frames][0] = sequence; rows[frames][1] = monotonicNanos;
        open = true;
    }

    public void set(int metric, long value) {
        requireMetric(metric, value);
        require(observedEpoch[metric] != epoch, "duplicate profiler metric set");
        rows[frames][metric + 2] = value; observedEpoch[metric] = epoch;
    }

    public void add(int metric, long value) {
        requireMetric(metric, value);
        rows[frames][metric + 2] = Math.addExact(rows[frames][metric + 2], value);
        observedEpoch[metric] = epoch;
    }

    public void maximum(int metric, long value) {
        requireMetric(metric, value);
        rows[frames][metric + 2] = Math.max(rows[frames][metric + 2], value);
        observedEpoch[metric] = epoch;
    }

    public void endFrame() {
        require(open, "profiler frame is not open");
        for (int metric = 0; metric < schema.size(); metric++)
            require(observedEpoch[metric] == epoch,
                    "unobserved profiler metric: " + schema.metric(metric).name());
        frames++; epoch++; open = false;
    }

    public FrameCensus snapshot() {
        require(!open && frames > 0, "profiler snapshot requires complete frames");
        long[][] copy = new long[frames][];
        System.arraycopy(rows, 0, copy, 0, frames);
        return FrameCensus.of(schema.metricNames(), copy);
    }

    public int metric(String name) { return schema.index(name); }
    public int frames() { return frames; }
    public int capacity() { return rows.length; }

    private void requireMetric(int metric, long value) {
        require(open, "profiler frame is not open");
        require(metric >= 0 && metric < schema.size(), "invalid profiler metric index");
        require(value >= 0L, "negative profiler metric value");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
