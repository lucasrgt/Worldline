package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.testapi.NativeEntityDynamicsObservation;

/** Equatable native measurements; a paired control is retained for every promoted claim. */
public final class NativeEntityDynamicsEvidence {
    private final List<NativeEntityDynamicsObservation> observations;
    NativeEntityDynamicsEvidence(List<NativeEntityDynamicsObservation> observations) {
        this.observations = Collections.unmodifiableList(new ArrayList<>(observations));
    }
    public List<NativeEntityDynamicsObservation> observations() { return observations; }
    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.native-entity-dynamics-evidence.v1\n");
        for (NativeEntityDynamicsObservation observation : observations) {
            value.append(observation.canonical()).append('\n');
        }
        return value.toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof NativeEntityDynamicsEvidence
                && canonical().equals(((NativeEntityDynamicsEvidence) other).canonical());
    }
    @Override public int hashCode() { return canonical().hashCode(); }
}
