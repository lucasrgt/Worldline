package worldline.testkit;

import java.util.Map;
import java.util.TreeMap;

/** Immutable equatable evidence for a complete entity context family. */
public final class EntityStateEvidence {
    private final String canonical;
    EntityStateEvidence(String family, Map<String, ?> values) {
        StringBuilder result = new StringBuilder("v1|entity-state|").append(family).append('\n');
        for (Map.Entry<String, ?> entry : new TreeMap<>(values).entrySet()) {
            result.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        canonical = result.toString();
    }
    public String canonical() { return canonical; }
    @Override public boolean equals(Object other) {
        return other instanceof EntityStateEvidence && canonical.equals(((EntityStateEvidence) other).canonical);
    }
    @Override public int hashCode() { return canonical.hashCode(); }
}
