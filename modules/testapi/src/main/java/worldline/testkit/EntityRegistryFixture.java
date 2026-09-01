package worldline.testkit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Expands one deterministic EntityList capture into universal per-entity claims. */
public final class EntityRegistryFixture {
    private EntityRegistryFixture() { }

    public static EntityRegistryEvidence execute(EntityConformancePlan plan,
            EntityRegistryScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("entity registry");
        List<EntityConformanceCase> claims = claims(plan);
        List<EntityRegistryObservation> first = observed(scenario);
        List<EntityRegistryObservation> second = observed(scenario);
        require(first.equals(second), "entity registry capture is not deterministic");
        Map<String, EntityRegistryObservation> bySubject = index(first);
        require(bySubject.size() == claims.size(), "entity registry subject count drifted");
        List<EntityRegistryObservation> ordered = new ArrayList<EntityRegistryObservation>();
        for (EntityConformanceCase claim : claims) {
            EntityRegistryObservation observation = bySubject.remove(claim.profile().subject());
            require(observation != null, "entity registry subject is absent: " + claim.claimId());
            ordered.add(observation);
        }
        require(bySubject.isEmpty(), "entity registry contains unplanned subjects");
        return new EntityRegistryEvidence(claims, ordered);
    }

    private static List<EntityConformanceCase> claims(EntityConformancePlan plan) {
        List<EntityConformanceCase> result = new ArrayList<EntityConformanceCase>();
        for (EntityConformanceCase claim : plan.cases()) {
            if (!"registry-presence".equals(claim.template().id())) continue;
            require(claim.layer() == ConformanceLayer.UNIVERSAL,
                    "registry-presence must use the universal layer");
            result.add(claim);
        }
        require(!result.isEmpty(), "registry-presence plan is absent");
        return result;
    }

    private static List<EntityRegistryObservation> observed(EntityRegistryScenario scenario) {
        List<EntityRegistryObservation> result = scenario.observe();
        require(result != null && !result.isEmpty(), "entity registry observation is absent");
        return new ArrayList<EntityRegistryObservation>(result);
    }

    private static Map<String, EntityRegistryObservation> index(
            List<EntityRegistryObservation> observations) {
        Map<String, EntityRegistryObservation> result =
                new LinkedHashMap<String, EntityRegistryObservation>();
        for (EntityRegistryObservation observation : observations) {
            require(observation != null && result.put(observation.subject(), observation) == null,
                    "duplicate entity registry subject");
        }
        return result;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
