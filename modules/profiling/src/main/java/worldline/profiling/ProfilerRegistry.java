package worldline.profiling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds one closed capability schema and stable handles before capture starts. */
public final class ProfilerRegistry {
    private final ProfilerSchema schema;
    private final Map<String, Handle> handles;

    private ProfilerRegistry(ProfilerSchema schema) {
        this.schema = schema;
        Map<String, Handle> result = new LinkedHashMap<String, Handle>();
        for (int index = 0; index < schema.size(); index++) {
            ProfilerMetric metric = schema.metric(index);
            result.put(metric.name(), new Handle(this, index, metric));
        }
        handles = Collections.unmodifiableMap(result);
    }

    public static Builder builder() { return new Builder(); }
    public ProfilerSchema schema() { return schema; }
    public boolean supports(String name) { return handles.containsKey(name); }

    public Handle require(String name) {
        Handle handle = handles.get(name);
        if (handle == null) throw new IllegalArgumentException("unsupported profiler metric: " + name);
        return handle;
    }

    public Handle optional(String name) { return handles.get(name); }

    /** Immutable index token avoids name lookup and allocation on instrumented paths. */
    public static final class Handle {
        private final ProfilerRegistry registry;
        private final int index;
        private final ProfilerMetric metric;
        private Handle(ProfilerRegistry registry, int index, ProfilerMetric metric) {
            this.registry = registry; this.index = index; this.metric = metric;
        }
        public ProfilerMetric metric() { return metric; }
        int index(ProfilerRegistry expected) {
            if (registry != expected) throw new IllegalArgumentException("foreign profiler handle");
            return index;
        }
    }

    public static final class Builder {
        private final Map<String, ProfilerMetric> metrics =
                new LinkedHashMap<String, ProfilerMetric>();

        public Builder support(String name) {
            ProfilerSchema catalog = WorldlineProfilerMetrics.standardSchema();
            return add(catalog.metric(catalog.index(name)));
        }

        public Builder support(String... names) {
            if (names == null) throw new NullPointerException("profiler metric names");
            for (String name : names) support(name);
            return this;
        }

        public Builder extension(ProfilerMetric metric) {
            if (metric == null || !metric.extensionOwned())
                throw new IllegalArgumentException("profiler extension ownership");
            return add(metric);
        }

        public ProfilerRegistry build() {
            if (metrics.isEmpty()) throw new IllegalStateException("empty profiler registry");
            List<ProfilerMetric> ordered = new ArrayList<ProfilerMetric>(metrics.values());
            return new ProfilerRegistry(ProfilerSchema.of(ordered));
        }

        private Builder add(ProfilerMetric metric) {
            if (metrics.put(metric.name(), metric) != null)
                throw new IllegalArgumentException("duplicate profiler capability: " + metric.name());
            return this;
        }
    }
}
