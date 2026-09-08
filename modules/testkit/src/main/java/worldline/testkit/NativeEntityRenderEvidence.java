package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.testapi.NativeEntityRenderObservation;

/** Equatable native render evidence; machine paths and class-loader identities are excluded. */
public final class NativeEntityRenderEvidence {
    private final List<NativeEntityRenderObservation> observations;

    NativeEntityRenderEvidence(List<NativeEntityRenderObservation> observations) {
        this.observations = Collections.unmodifiableList(
                new ArrayList<NativeEntityRenderObservation>(observations));
    }

    public List<NativeEntityRenderObservation> observations() { return observations; }
    public String canonical() {
        StringBuilder text = new StringBuilder("schema=worldline.native-entity-render.v1\n");
        text.append("scene=canonical-pose,full-bright,partial-tick-0,body-only\n");
        text.append("claims=").append(observations.size()).append('\n');
        for (int index = 0; index < observations.size(); index++) {
            NativeEntityRenderObservation row = observations.get(index);
            text.append("claim.").append(index + 1).append('=').append(row.subject().subject())
                    .append("#native-render|").append(row.subject().layer()).append('\n');
            text.append("render.").append(index + 1).append('=').append(row.canonical()).append('\n');
        }
        return text.toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof NativeEntityRenderEvidence
                && canonical().equals(((NativeEntityRenderEvidence) other).canonical());
    }
    @Override public int hashCode() { return canonical().hashCode(); }
}
