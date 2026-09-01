package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Canonical public evidence for native concrete-entity persistence. */
public final class EntityPersistenceEvidence {
    private final List<EntityPersistenceObservation> observations;

    EntityPersistenceEvidence(List<EntityPersistenceObservation> observations) {
        this.observations = Collections.unmodifiableList(
                new ArrayList<EntityPersistenceObservation>(observations));
    }

    public List<EntityPersistenceObservation> observations() { return observations; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.entity-persistence-evidence.v1\n");
        value.append("claims=").append(observations.size()).append('\n');
        for (int index = 0; index < observations.size(); index++) {
            String key = String.format("%03d", index + 1);
            EntityPersistenceObservation observation = observations.get(index);
            value.append("claim.").append(key).append('=').append(observation.subject())
                    .append("#save-reload|UNIVERSAL\n");
            value.append("row.").append(key).append('=').append(observation.canonical())
                    .append('\n');
        }
        return value.toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof EntityPersistenceEvidence
                && canonical().equals(((EntityPersistenceEvidence) other).canonical());
    }
    @Override public int hashCode() { return canonical().hashCode(); }
}
