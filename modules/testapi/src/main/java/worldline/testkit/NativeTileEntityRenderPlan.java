package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validated plan for one native tile-entity rendering family. */
public final class NativeTileEntityRenderPlan {
    private final String family;
    private final List<NativeTileEntityRenderSubject> subjects;

    public NativeTileEntityRenderPlan(String family,
            List<NativeTileEntityRenderSubject> subjects) {
        if (family == null || !family.matches("[a-z0-9][a-z0-9-]{0,62}")
                || subjects == null || subjects.isEmpty()) {
            throw new IllegalArgumentException("invalid native tile-render plan");
        }
        List<NativeTileEntityRenderSubject> copy = new ArrayList<>(subjects);
        Set<String> ids = new HashSet<>();
        Set<Integer> legacyIds = new HashSet<>();
        for (NativeTileEntityRenderSubject subject : copy) {
            if (subject == null || !ids.add(subject.subject())
                    || !legacyIds.add(Integer.valueOf(subject.legacyId()))) {
                throw new IllegalArgumentException("duplicate native tile-render subject");
            }
        }
        this.family = family;
        this.subjects = Collections.unmodifiableList(copy);
    }

    public String family() { return family; }
    public List<NativeTileEntityRenderSubject> subjects() { return subjects; }
}
