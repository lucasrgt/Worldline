package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Validates deterministic native persistence observations for concrete entities and returns
 * subject-sorted evidence suitable for exact comparisons across fresh runtime captures.
 */
public final class EntityPersistenceFixture {
    private EntityPersistenceFixture() { }

    public static EntityPersistenceEvidence execute(EntityPersistenceScenario scenario) {
        if (scenario == null) throw new NullPointerException("entity persistence scenario");
        List<EntityPersistenceObservation> first = observed(scenario);
        require(first.equals(observed(scenario)),
                "entity persistence capture is not deterministic");
        Map<String, EntityPersistenceObservation> indexed = new TreeMap<>();
        for (EntityPersistenceObservation observation : first) {
            require(observation != null
                            && indexed.put(observation.subject(), observation) == null,
                    "duplicate entity persistence subject");
            require(observation.reconstructed() && observation.typeExact()
                            && observation.commonStateExact() && observation.nbtExact(),
                    "native entity persistence differs: " + observation.subject());
        }
        return new EntityPersistenceEvidence(
                new ArrayList<EntityPersistenceObservation>(indexed.values()));
    }

    private static List<EntityPersistenceObservation> observed(
            EntityPersistenceScenario scenario) {
        List<EntityPersistenceObservation> result = scenario.observe();
        require(result != null && !result.isEmpty(), "entity persistence observation absent");
        return new ArrayList<EntityPersistenceObservation>(result);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
