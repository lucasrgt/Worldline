package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validated universal plan for one native 3D inventory render family. */
public final class NativeBlockRenderPlan {
    private final String family;
    private final List<NativeBlockRenderSubject> subjects;

    public NativeBlockRenderPlan(String family, List<NativeBlockRenderSubject> subjects) {
        if (family == null || !family.matches("[a-z0-9][a-z0-9-]{0,62}")) {
            throw new IllegalArgumentException("invalid native render family");
        }
        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalArgumentException("native render subjects are empty");
        }
        List<NativeBlockRenderSubject> copy = new ArrayList<NativeBlockRenderSubject>(subjects);
        Set<String> ids = new HashSet<String>();
        Set<Integer> legacyIds = new HashSet<Integer>();
        for (NativeBlockRenderSubject subject : copy) {
            if (subject == null || !ids.add(subject.subject())
                    || !legacyIds.add(Integer.valueOf(subject.legacyId()))) {
                throw new IllegalArgumentException("null or duplicate native render subject");
            }
        }
        this.family = family;
        this.subjects = Collections.unmodifiableList(copy);
    }

    public String family() { return family; }
    public List<NativeBlockRenderSubject> subjects() { return subjects; }
}
