package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Deterministic three-layer expansion of persistent entity profiles and dimensions. */
public final class EntityConformancePlan {
    private final List<EntityConformanceCase> cases;

    public EntityConformancePlan(List<EntityConformanceProfile> profiles,
            List<EntityConformanceTemplate> templates) {
        if (profiles == null || profiles.isEmpty() || templates == null || templates.isEmpty()) {
            throw new IllegalArgumentException("empty conformance plan");
        }
        Set<String> subjects = new HashSet<String>();
        Set<String> dimensions = new HashSet<String>();
        for (EntityConformanceProfile profile : profiles) {
            if (profile == null || !subjects.add(profile.subject())) {
                throw new IllegalArgumentException("duplicate profile");
            }
        }
        for (EntityConformanceTemplate template : templates) {
            if (template == null || !dimensions.add(template.id())) {
                throw new IllegalArgumentException("duplicate template");
            }
        }
        List<EntityConformanceCase> expanded = new ArrayList<EntityConformanceCase>();
        for (EntityConformanceProfile profile : profiles) {
            for (EntityConformanceTemplate template : templates) {
                expanded.add(new EntityConformanceCase(profile, template, profile.layer(template)));
            }
        }
        cases = Collections.unmodifiableList(expanded);
    }

    public List<EntityConformanceCase> cases() {
        return cases;
    }

    public EntityConformanceCase caseFor(String subject, String template) {
        if (subject == null || template == null) throw new NullPointerException("claim key");
        for (EntityConformanceCase value : cases) {
            if (value.profile().subject().equals(subject)
                    && value.template().id().equals(template)) return value;
        }
        throw new IllegalArgumentException("conformance claim is absent: "
                + subject + "#" + template);
    }
}
