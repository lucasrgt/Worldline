package worldline.api;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Immutable named counters exposed by a controlled mod runtime. */
public final class RuntimeWorkSnapshot {
    private final long generation;
    private final Map<String, Long> counters;

    public RuntimeWorkSnapshot(long generation, Map<String, Long> counters) {
        if (generation < 0 || counters == null) throw new IllegalArgumentException("work snapshot");
        TreeMap<String, Long> copy = new TreeMap<String, Long>();
        for (Map.Entry<String, Long> entry : counters.entrySet()) {
            String name = entry.getKey(); Long value = entry.getValue();
            if (name == null || !name.matches("[a-z][a-z0-9_.-]*") || value == null || value < 0)
                throw new IllegalArgumentException("work counter");
            copy.put(name, value);
        }
        this.generation = generation; this.counters = Collections.unmodifiableMap(copy);
    }
    public long generation() { return generation; }
    public Map<String, Long> counters() { return counters; }
    public long counter(String name) {
        Long value = counters.get(name);
        if (value == null) throw new IllegalArgumentException("unknown work counter " + name);
        return value.longValue();
    }
    public long increaseFrom(RuntimeWorkSnapshot earlier, String name) {
        if (earlier == null) throw new NullPointerException("earlier");
        long change = counter(name) - earlier.counter(name);
        if (change < 0) throw new IllegalStateException("work counter moved backwards " + name);
        return change;
    }
}
