package worldline.profiling;

import java.util.Arrays;

/** Deterministic aggregates over one sealed profiler run. */
public final class ProfilerSummary {
    private final ProfilerRun run;

    public ProfilerSummary(ProfilerRun run) {
        if (run == null) throw new NullPointerException("profiler run");
        this.run = run;
    }

    public int frames() { return run.census().frames(); }
    public long total(String metric) {
        long total = 0L;
        for (int frame = 0; frame < frames(); frame++)
            total = Math.addExact(total, value(frame, metric));
        return total;
    }
    public long mean(String metric) { return total(metric) / frames(); }
    public long maximum(String metric) {
        long result = 0L;
        for (int frame = 0; frame < frames(); frame++)
            result = Math.max(result, value(frame, metric));
        return result;
    }
    public int worstFrame(String metric) {
        int result = 0;
        for (int frame = 1; frame < frames(); frame++)
            if (value(frame, metric) > value(result, metric)) result = frame;
        return result;
    }
    public long percentile(String metric, int numerator, int denominator) {
        require(numerator > 0 && denominator > 0 && numerator <= denominator,
                "invalid profiler percentile");
        long[] sorted = new long[frames()];
        for (int frame = 0; frame < frames(); frame++) sorted[frame] = value(frame, metric);
        Arrays.sort(sorted);
        int rank = (int) (((long) numerator * sorted.length + denominator - 1L) / denominator);
        return sorted[Math.max(1, rank) - 1];
    }
    public long countAtLeast(String metric, long threshold) {
        require(threshold >= 0L, "negative profiler threshold");
        long count = 0L;
        for (int frame = 0; frame < frames(); frame++)
            if (value(frame, metric) >= threshold) count++;
        return count;
    }
    public long unattributedWall(int frame) {
        long wall = value(frame, WorldlineProfilerMetrics.FRAME_WALL), attributed = 0L;
        for (ProfilerMetric metric : run.schema().metrics()) {
            if (metric.causality() == ProfilerMetric.Causality.TOP_LEVEL)
                attributed = Math.addExact(attributed, value(frame, metric.name()));
        }
        return Math.max(0L, wall - attributed);
    }
    public boolean steadyQualified(String... activityMetrics) {
        require(activityMetrics != null && activityMetrics.length > 0,
                "steady qualification requires activity metrics");
        if (run.mode() != ProfilerRun.Mode.STEADY) return false;
        for (String metric : activityMetrics) if (total(metric) != 0L) return false;
        return true;
    }
    private long value(int frame, String metric) { return run.census().value(frame, metric); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
