package worldline.testkit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Validates deterministic archetype/singular physical envelopes for concrete entities. */
public final class EntityPhysicalEnvelopeFixture {
    private EntityPhysicalEnvelopeFixture() { }

    public static EntityPhysicalEnvelopeEvidence execute(EntityConformancePlan plan,
            EntityPhysicalEnvelopeScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("entity envelope");
        List<EntityConformanceCase> claims = claims(plan);
        List<EntityPhysicalEnvelopeObservation> first = observed(scenario);
        List<EntityPhysicalEnvelopeObservation> second = observed(scenario);
        require(first.equals(second), "entity physical envelope capture is not deterministic");
        Map<String, EntityPhysicalEnvelopeObservation> bySubject = index(first);
        require(bySubject.size() == claims.size(), "entity physical envelope count drifted");
        List<EntityPhysicalEnvelopeObservation> ordered =
                new ArrayList<EntityPhysicalEnvelopeObservation>();
        for (EntityConformanceCase claim : claims) {
            EntityPhysicalEnvelopeObservation observation = bySubject.remove(
                    claim.profile().subject());
            require(observation != null, "entity physical envelope is absent: " + claim.claimId());
            require(observation.centered() && observation.vertical(),
                    "native AABB differs from the declared dimensions: " + claim.claimId());
            ordered.add(observation);
        }
        require(bySubject.isEmpty(), "entity physical envelope contains unplanned subjects");
        return new EntityPhysicalEnvelopeEvidence(claims, ordered);
    }

    private static List<EntityConformanceCase> claims(EntityConformancePlan plan) {
        List<EntityConformanceCase> result = new ArrayList<EntityConformanceCase>();
        boolean archetype = false, singular = false;
        for (EntityConformanceCase claim : plan.cases()) {
            if (!"collision-shape".equals(claim.template().id())) continue;
            require(claim.layer() != ConformanceLayer.UNIVERSAL,
                    "collision-shape cannot use the universal layer");
            archetype |= claim.layer() == ConformanceLayer.ARCHETYPE;
            singular |= claim.layer() == ConformanceLayer.SINGULAR;
            result.add(claim);
        }
        require(!result.isEmpty(), "collision-shape plan is absent");
        require(archetype && singular, "physical envelope plan must exercise both routed layers");
        return result;
    }

    private static List<EntityPhysicalEnvelopeObservation> observed(
            EntityPhysicalEnvelopeScenario scenario) {
        List<EntityPhysicalEnvelopeObservation> result = scenario.observe();
        require(result != null && !result.isEmpty(), "entity physical envelope observation absent");
        return new ArrayList<EntityPhysicalEnvelopeObservation>(result);
    }

    private static Map<String, EntityPhysicalEnvelopeObservation> index(
            List<EntityPhysicalEnvelopeObservation> observations) {
        Map<String, EntityPhysicalEnvelopeObservation> result =
                new LinkedHashMap<String, EntityPhysicalEnvelopeObservation>();
        for (EntityPhysicalEnvelopeObservation observation : observations) {
            require(observation != null && result.put(observation.subject(), observation) == null,
                    "duplicate entity physical envelope subject");
        }
        return result;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
