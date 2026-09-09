package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.testapi.ParticleLifecycleObservation;

/** Equatable evidence for the fixed eight-tick particle scene and native terminal removal. */
public final class ParticleLifecycleEvidence {
    private final List<ParticleLifecycleObservation> observations;
    ParticleLifecycleEvidence(List<ParticleLifecycleObservation> observations) {
        this.observations = Collections.unmodifiableList(new ArrayList<ParticleLifecycleObservation>(observations));
    }
    public List<ParticleLifecycleObservation> observations() { return observations; }
    public String canonical() {
        StringBuilder result = new StringBuilder("schema=worldline.client-particle-render.v1\n");
        result.append("scene=controlled-age-8,air,full-bright,partial-tick-0.5\n");
        for (ParticleLifecycleObservation row : observations) result.append(row.canonical()).append('\n');
        return result.toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof ParticleLifecycleEvidence
                && canonical().equals(((ParticleLifecycleEvidence) other).canonical());
    }
    @Override public int hashCode() { return canonical().hashCode(); }
}
