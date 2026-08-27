package worldline.testkit;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Data-only routing profile for one versioned block-registry subject. */
public final class BlockConformanceProfile {
    private final String subject;
    private final List<String> archetypes;
    private final boolean singular;
    private final Map<String, ConformanceLayer> overrides;

    public BlockConformanceProfile(String subject, List<String> archetypes, boolean singular,
            Map<String, ConformanceLayer> overrides) {
        if (subject == null || !subject.matches("b[0-9]+\\.[0-9]+\\.[0-9]+:block/[0-9]{3}")) {
            throw new IllegalArgumentException("subject");
        }
        if (archetypes == null || archetypes.isEmpty()) {
            throw new IllegalArgumentException("archetypes");
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String archetype : archetypes) {
            if (archetype == null || !archetype.matches("[a-z][a-z0-9-]{0,62}")
                    || !unique.add(archetype)) {
                throw new IllegalArgumentException("archetypes");
            }
        }
        this.subject = subject;
        this.archetypes = List.copyOf(unique);
        this.singular = singular;
        this.overrides = copyOverrides(overrides);
    }

    public String subject() {
        return subject;
    }

    public List<String> archetypes() {
        return archetypes;
    }

    public boolean singular() {
        return singular;
    }

    ConformanceLayer layer(BlockConformanceTemplate template) {
        ConformanceLayer override = overrides.get(template.id());
        if (override != null) {
            return override;
        }
        if (singular && template.defaultLayer() == ConformanceLayer.ARCHETYPE) {
            return ConformanceLayer.SINGULAR;
        }
        return template.defaultLayer();
    }

    private static Map<String, ConformanceLayer> copyOverrides(
            Map<String, ConformanceLayer> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, ConformanceLayer> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key == null || !key.matches("[a-z][a-z0-9-]{0,62}")) {
                throw new IllegalArgumentException("override");
            }
            if (result.put(key, Objects.requireNonNull(value, "override")) != null) {
                throw new IllegalArgumentException("override");
            }
        });
        return Map.copyOf(result);
    }
}
