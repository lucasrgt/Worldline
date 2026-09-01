package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Canonical public evidence for native special world block rendering. */
public final class NativeWorldBlockRenderEvidence {
    private final String family;
    private final List<NativeWorldBlockRenderObservation> observations;

    NativeWorldBlockRenderEvidence(String family,
            List<NativeWorldBlockRenderObservation> observations) {
        this.family = family;
        this.observations = Collections.unmodifiableList(new ArrayList<>(observations));
    }

    public String family() { return family; }
    public List<NativeWorldBlockRenderObservation> observations() { return observations; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.native-world-block-render.v1\n");
        value.append("family=").append(family).append('\n');
        value.append("subjects=").append(observations.size()).append('\n');
        for (int index = 0; index < observations.size(); index++) {
            NativeWorldBlockRenderObservation row = observations.get(index);
            value.append("claim.").append(index + 1).append('=')
                    .append(row.subject()).append("#native-render|ARCHETYPE\n");
            value.append("render.").append(index + 1).append('=')
                    .append(row.canonical()).append('\n');
        }
        return value.toString();
    }
}
