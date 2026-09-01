package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Canonical public evidence for one native 3D inventory render family. */
public final class NativeBlockRenderEvidence {
    private final String family;
    private final List<NativeBlockRenderObservation> observations;

    NativeBlockRenderEvidence(String family, List<NativeBlockRenderObservation> observations) {
        this.family = family;
        this.observations = Collections.unmodifiableList(
                new ArrayList<NativeBlockRenderObservation>(observations));
    }

    public String family() { return family; }
    public List<NativeBlockRenderObservation> observations() { return observations; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.native-block-render.v1\n");
        value.append("family=").append(family).append('\n');
        value.append("subjects=").append(observations.size()).append('\n');
        for (int index = 0; index < observations.size(); index++) {
            NativeBlockRenderObservation row = observations.get(index);
            value.append("claim.").append(index + 1).append('=')
                    .append(row.subject()).append("#native-render|ARCHETYPE\n");
            value.append("render.").append(index + 1).append('=')
                    .append(row.canonical()).append('\n');
        }
        return value.toString();
    }
}
