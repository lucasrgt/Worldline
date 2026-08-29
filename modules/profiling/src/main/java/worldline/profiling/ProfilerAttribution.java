package worldline.profiling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Non-additive causal classification for nested and overlapping stage metrics. */
public final class ProfilerAttribution {
    private final ProfilerRun run;
    private final List<Group> groups;

    public ProfilerAttribution(ProfilerRun run, List<Group> groups) {
        if (run == null || groups == null) throw new NullPointerException("profiler attribution");
        require(!groups.isEmpty(), "profiler attribution requires groups");
        for (Group group : groups)
            for (String metric : group.metrics) require(run.schema().contains(metric),
                    "attribution metric is not captured: " + metric);
        this.run = run;
        this.groups = Collections.unmodifiableList(new ArrayList<Group>(groups));
    }

    public static ProfilerAttribution standard(ProfilerRun run) {
        if (run == null) throw new NullPointerException("profiler run");
        require(run.schema().contains(WorldlineProfilerMetrics.FRAME_WALL),
                "standard attribution requires frame wall time");
        List<Group> groups = new ArrayList<Group>();
        supported(groups, run, "client-tick", WorldlineProfilerMetrics.CLIENT_TICK);
        supported(groups, run, "world", WorldlineProfilerMetrics.WORLD_TICK,
                    WorldlineProfilerMetrics.WORLD_ENTITIES,
                    WorldlineProfilerMetrics.WORLD_BLOCKS,
                    WorldlineProfilerMetrics.WORLD_WEATHER);
        supported(groups, run, "chunk", WorldlineProfilerMetrics.CHUNK_LOOKUP,
                    WorldlineProfilerMetrics.CHUNK_LOAD,
                    WorldlineProfilerMetrics.CHUNK_GENERATE,
                    WorldlineProfilerMetrics.CHUNK_POPULATE,
                    WorldlineProfilerMetrics.CHUNK_SAVE,
                    WorldlineProfilerMetrics.CHUNK_FLUSH,
                    WorldlineProfilerMetrics.CHUNK_COMPILE,
                    WorldlineProfilerMetrics.CHUNK_REBUILD);
        supported(groups, run, "render", WorldlineProfilerMetrics.RENDER_CAMERA,
                    WorldlineProfilerMetrics.RENDER_WORLD,
                    WorldlineProfilerMetrics.RENDER_TERRAIN,
                    WorldlineProfilerMetrics.RENDER_ENTITIES,
                    WorldlineProfilerMetrics.RENDER_GUI);
        supported(groups, run, "display", WorldlineProfilerMetrics.DISPLAY_PRESENT);
        supported(groups, run, "gpu", WorldlineProfilerMetrics.GPU_FRAME,
                WorldlineProfilerMetrics.GPU_WAIT);
        supported(groups, run, "gc", WorldlineProfilerMetrics.GC_PAUSE);
        supported(groups, run, "jit", WorldlineProfilerMetrics.JIT_COMPILATION);
        return new ProfilerAttribution(run, groups);
    }

    public Result classify(int frame, long absoluteFloorNanos, int numerator, int denominator) {
        require(absoluteFloorNanos >= 0L && numerator > 0 && denominator > 0
                && numerator <= denominator, "invalid profiler attribution threshold");
        long frameNanos = run.census().value(frame, WorldlineProfilerMetrics.FRAME_WALL);
        long threshold = Math.max(absoluteFloorNanos, ceiling(frameNanos, numerator, denominator));
        List<String> material = new ArrayList<String>();
        List<Long> values = new ArrayList<Long>();
        for (Group group : groups) {
            long value = group.maximum(run.census(), frame);
            if (value >= threshold) { material.add(group.name); values.add(Long.valueOf(value)); }
        }
        return new Result(frame, threshold, material, values);
    }

    public static final class Group {
        private final String name;
        private final List<String> metrics;
        private Group(String name, List<String> metrics) { this.name = name; this.metrics = metrics; }
        public static Group of(String name, String... metrics) {
            require(name != null && name.matches("[a-z][a-z0-9-]{0,63}"),
                    "invalid profiler attribution group");
            require(metrics != null && metrics.length > 0, "empty profiler attribution group");
            return new Group(name, Collections.unmodifiableList(
                    java.util.Arrays.asList(metrics.clone())));
        }
        private long maximum(FrameCensus census, int frame) {
            long result = 0L;
            for (String metric : metrics) result = Math.max(result, census.value(frame, metric));
            return result;
        }
    }

    public static final class Result {
        private final int frame;
        private final long threshold;
        private final List<String> causes;
        private final List<Long> values;
        private Result(int frame, long threshold, List<String> causes, List<Long> values) {
            this.frame = frame; this.threshold = threshold;
            this.causes = Collections.unmodifiableList(causes);
            this.values = Collections.unmodifiableList(values);
        }
        public int frame() { return frame; }
        public long thresholdNanos() { return threshold; }
        public List<String> causes() { return causes; }
        public long valueNanos(int index) { return values.get(index).longValue(); }
        public boolean unknown() { return causes.isEmpty(); }
        public boolean mixed() { return causes.size() > 1; }
    }

    private static long ceiling(long value, int numerator, int denominator) {
        return Math.addExact(Math.multiplyExact(value / denominator, numerator),
                ((value % denominator) * numerator + denominator - 1L) / denominator);
    }
    private static void supported(List<Group> target, ProfilerRun run, String name,
            String... candidates) {
        List<String> metrics = new ArrayList<String>();
        for (String metric : candidates) if (run.schema().contains(metric)) metrics.add(metric);
        if (!metrics.isEmpty()) target.add(Group.of(name, metrics.toArray(new String[metrics.size()])));
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
