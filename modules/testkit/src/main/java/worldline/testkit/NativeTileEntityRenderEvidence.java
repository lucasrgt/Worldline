package worldline.testkit;
import worldline.testapi.NativeTileEntityRenderObservation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Canonical public evidence for native tile-entity rendering. */
public final class NativeTileEntityRenderEvidence {
    private final String family;
    private final List<NativeTileEntityRenderObservation> observations;

    NativeTileEntityRenderEvidence(String family,
            List<NativeTileEntityRenderObservation> observations) {
        this.family = family;
        this.observations = Collections.unmodifiableList(new ArrayList<>(observations));
    }

    public String family() { return family; }
    public List<NativeTileEntityRenderObservation> observations() { return observations; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.native-tile-render.v1\n");
        value.append("family=").append(family).append('\n');
        value.append("subjects=").append(observations.size()).append('\n');
        for (int index = 0; index < observations.size(); index++) {
            NativeTileEntityRenderObservation row = observations.get(index);
            value.append("claim.").append(index + 1).append('=')
                    .append(row.subject().subject()).append("#native-render|")
                    .append(row.subject().layer()).append('\n');
            value.append("render.").append(index + 1).append('=')
                    .append(row.canonical()).append('\n');
        }
        return value.toString();
    }
}
