package worldline.test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Fail-closed mutation manifest used by adapter qualification. */
public final class MutationCoverage {
    private final Map<String, MutationBoundary> boundaries;

    public MutationCoverage(Collection<MutationBoundary> values) {
        if (values == null) throw new NullPointerException("boundaries");
        Map<String, MutationBoundary> copy = new LinkedHashMap<String, MutationBoundary>();
        for (MutationBoundary boundary : values) {
            if (boundary == null || copy.put(boundary.id(), boundary) != null)
                throw new IllegalArgumentException("duplicate or null mutation boundary");
        }
        boundaries = Collections.unmodifiableMap(copy);
    }
    public Set<String> ids() { return boundaries.keySet(); }
    public MutationBoundary boundary(String id) {
        MutationBoundary value = boundaries.get(id);
        if (value == null) throw new IllegalArgumentException("unmapped mutation boundary " + id);
        return value;
    }
    public void requireAll(Collection<String> required) {
        if (required == null) throw new NullPointerException("required");
        for (String id : required) boundary(id);
    }
}
