package worldline.testkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Expands profiles and dimensions into universal, archetype, and singular cases. */
public final class BlockConformancePlan {
    private final List<BlockConformanceCase> cases;

    public BlockConformancePlan(List<BlockConformanceProfile> profiles,
            List<BlockConformanceTemplate> templates) {
        if (profiles == null || profiles.isEmpty() || templates == null || templates.isEmpty()) {
            throw new IllegalArgumentException("empty conformance plan");
        }
        Set<String> subjects = new HashSet<>();
        Set<String> dimensions = new HashSet<>();
        for (BlockConformanceProfile profile : profiles) {
            if (profile == null || !subjects.add(profile.subject())) {
                throw new IllegalArgumentException("duplicate profile");
            }
        }
        for (BlockConformanceTemplate template : templates) {
            if (template == null || !dimensions.add(template.id())) {
                throw new IllegalArgumentException("duplicate template");
            }
        }
        List<BlockConformanceCase> expanded = new ArrayList<>();
        for (BlockConformanceProfile profile : profiles) {
            for (BlockConformanceTemplate template : templates) {
                expanded.add(new BlockConformanceCase(profile, template, profile.layer(template)));
            }
        }
        this.cases = List.copyOf(expanded);
    }

    public List<BlockConformanceCase> cases() {
        return cases;
    }
}
