package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Public expansion of profiles and dimensions into three-layer conformance cases. */
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
        this.cases = Collections.unmodifiableList(expanded);
    }

    public List<BlockConformanceCase> cases() {
        return cases;
    }

    public BlockConformanceCase caseFor(String subject, String template) {
        if (subject == null || template == null) throw new NullPointerException("claim key");
        for (BlockConformanceCase value : cases) {
            if (value.profile().subject().equals(subject)
                    && value.template().id().equals(template)) return value;
        }
        throw new IllegalArgumentException("conformance claim is absent: "
                + subject + "#" + template);
    }
}
