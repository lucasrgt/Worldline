package worldline.testkit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Expands one deterministic registry capture into universal per-block claims. */
public final class BlockRegistryFixture {
    private BlockRegistryFixture() { }

    public static BlockRegistryEvidence execute(BlockConformancePlan plan,
            BlockRegistryScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("block registry");
        List<BlockConformanceCase> claims = claims(plan);
        List<BlockRegistryObservation> first = observed(scenario);
        List<BlockRegistryObservation> second = observed(scenario);
        require(first.equals(second), "block registry capture is not deterministic");
        Map<String, BlockRegistryObservation> bySubject = index(first);
        require(bySubject.size() == claims.size(), "block registry subject count drifted");
        List<BlockRegistryObservation> ordered = new ArrayList<BlockRegistryObservation>();
        for (BlockConformanceCase claim : claims) {
            BlockRegistryObservation observation = bySubject.remove(claim.profile().subject());
            require(observation != null, "block registry subject is absent: " + claim.claimId());
            ordered.add(observation);
        }
        require(bySubject.isEmpty(), "block registry contains unplanned subjects");
        return new BlockRegistryEvidence(claims, ordered);
    }

    private static List<BlockConformanceCase> claims(BlockConformancePlan plan) {
        List<BlockConformanceCase> result = new ArrayList<BlockConformanceCase>();
        for (BlockConformanceCase claim : plan.cases()) {
            if (!"registry-presence".equals(claim.template().id())) continue;
            require(claim.layer() == ConformanceLayer.UNIVERSAL,
                    "registry-presence must use the universal layer");
            result.add(claim);
        }
        require(!result.isEmpty(), "registry-presence plan is absent");
        return result;
    }

    private static List<BlockRegistryObservation> observed(BlockRegistryScenario scenario) {
        List<BlockRegistryObservation> result = scenario.observe();
        require(result != null && !result.isEmpty(), "block registry observation is absent");
        return new ArrayList<BlockRegistryObservation>(result);
    }

    private static Map<String, BlockRegistryObservation> index(
            List<BlockRegistryObservation> observations) {
        Map<String, BlockRegistryObservation> result = new LinkedHashMap<String, BlockRegistryObservation>();
        for (BlockRegistryObservation observation : observations) {
            require(observation != null && result.put(observation.subject(), observation) == null,
                    "duplicate block registry subject");
        }
        return result;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
