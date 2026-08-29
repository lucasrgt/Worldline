package worldline.profiling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable ordered metric schema shared by runtime adapters and analyzers. */
public final class ProfilerSchema {
    public static final int MAX_METRICS = 256;
    private final List<ProfilerMetric> metrics;
    private final Map<String, Integer> indexes;

    private ProfilerSchema(List<ProfilerMetric> metrics, Map<String, Integer> indexes) {
        this.metrics = metrics; this.indexes = indexes;
    }

    public static ProfilerSchema of(List<ProfilerMetric> source) {
        if (source == null) throw new NullPointerException("profiler schema");
        require(!source.isEmpty() && source.size() <= MAX_METRICS,
                "profiler schema metric count");
        List<ProfilerMetric> metrics = new ArrayList<ProfilerMetric>(source.size());
        Map<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        for (ProfilerMetric metric : source) {
            if (metric == null) throw new NullPointerException("profiler metric");
            require(indexes.put(metric.name(), Integer.valueOf(metrics.size())) == null,
                    "duplicate profiler metric: " + metric.name());
            metrics.add(metric);
        }
        return new ProfilerSchema(Collections.unmodifiableList(metrics),
                Collections.unmodifiableMap(indexes));
    }

    public ProfilerSchema extend(List<ProfilerMetric> extension) {
        if (extension == null) throw new NullPointerException("profiler extension");
        List<ProfilerMetric> combined = new ArrayList<ProfilerMetric>(metrics);
        for (ProfilerMetric metric : extension) {
            require(metric != null && metric.extensionOwned(),
                    "extension metric must have a non-Worldline owner");
            combined.add(metric);
        }
        return of(combined);
    }

    public int size() { return metrics.size(); }
    public ProfilerMetric metric(int index) { return metrics.get(index); }
    public List<ProfilerMetric> metrics() { return metrics; }
    public boolean contains(String name) { return indexes.containsKey(name); }

    public int index(String name) {
        Integer index = indexes.get(name);
        if (index == null) throw new IllegalArgumentException("unknown profiler metric: " + name);
        return index.intValue();
    }

    public String[] metricNames() {
        String[] names = new String[metrics.size()];
        for (int index = 0; index < names.length; index++) names[index] = metric(index).name();
        return names;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
