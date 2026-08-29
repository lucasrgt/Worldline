package worldline.profiling;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** One sealed measurement window with typed schema, census, mode, and tags. */
public final class ProfilerRun {
    public enum Mode { STEADY, STREAMING, MIXED }
    private final ProfilerSchema schema;
    private final FrameCensus census;
    private final Mode mode;
    private final long startEpochMillis, endEpochMillis;
    private final Map<String, String> tags;

    private ProfilerRun(ProfilerSchema schema, FrameCensus census, Mode mode,
            long startEpochMillis, long endEpochMillis, Map<String, String> tags) {
        this.schema = schema; this.census = census; this.mode = mode;
        this.startEpochMillis = startEpochMillis; this.endEpochMillis = endEpochMillis;
        this.tags = tags;
    }

    public static ProfilerRun of(ProfilerSchema schema, FrameCensus census, Mode mode,
            long startEpochMillis, long endEpochMillis, Map<String, String> sourceTags) {
        if (schema == null || census == null || mode == null || sourceTags == null)
            throw new NullPointerException("profiler run");
        require(startEpochMillis >= 0L && endEpochMillis >= startEpochMillis,
                "invalid profiler epoch window");
        require(java.util.Arrays.equals(schema.metricNames(), census.metricNames()),
                "profiler schema and census differ");
        require(sourceTags.size() <= 64, "too many profiler tags");
        Map<String, String> tags = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : sourceTags.entrySet()) {
            String key = entry.getKey(), value = entry.getValue();
            require(key != null && key.matches("[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*"),
                    "invalid profiler tag: " + key);
            require(value != null && value.length() <= 1024
                    && value.indexOf('\n') < 0 && value.indexOf('\r') < 0,
                    "invalid profiler tag value: " + key);
            require(tags.put(key, value) == null, "duplicate profiler tag: " + key);
        }
        return new ProfilerRun(schema, census, mode, startEpochMillis, endEpochMillis,
                Collections.unmodifiableMap(tags));
    }

    public ProfilerSchema schema() { return schema; }
    public FrameCensus census() { return census; }
    public Mode mode() { return mode; }
    public long startEpochMillis() { return startEpochMillis; }
    public long endEpochMillis() { return endEpochMillis; }
    public Map<String, String> tags() { return tags; }
    public String tag(String name) { return tags.get(name); }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
