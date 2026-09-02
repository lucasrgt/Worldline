package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validated plan for one native special world-render family. */
public final class NativeWorldBlockRenderPlan {
    private final String family;
    private final List<NativeWorldBlockRenderSubject> subjects;

    public NativeWorldBlockRenderPlan(String family,
            List<NativeWorldBlockRenderSubject> subjects) {
        if (family == null || !family.matches("[a-z0-9][a-z0-9-]{0,62}")) {
            throw new IllegalArgumentException("invalid native world-render family");
        }
        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalArgumentException("native world-render subjects are empty");
        }
        List<NativeWorldBlockRenderSubject> copy = new ArrayList<>(subjects);
        Set<String> ids = new HashSet<>();
        Set<Integer> legacyIds = new HashSet<>();
        for (NativeWorldBlockRenderSubject subject : copy) {
            if (subject == null || !ids.add(subject.subject())
                    || !legacyIds.add(Integer.valueOf(subject.legacyId()))) {
                throw new IllegalArgumentException("null or duplicate native world-render subject");
            }
        }
        this.family = family;
        this.subjects = Collections.unmodifiableList(copy);
    }

    public String family() { return family; }
    public List<NativeWorldBlockRenderSubject> subjects() { return subjects; }
}
