package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Validates caller-owned scheduled, random, and tile tick observations. */
public final class BlockTickPolicyFixture {
    private BlockTickPolicyFixture() { }

    public static List<BlockTickPolicyEvidence> execute(List<BlockTickPolicyScenario> scenarios,
            List<BlockTickPolicyObservation> observations) {
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("tick-policy scenarios are empty");
        }
        if (observations == null || observations.size() != scenarios.size()) {
            throw new IllegalArgumentException("tick-policy observation census drifted");
        }
        Map<String, BlockTickPolicyObservation> byId = new LinkedHashMap<String,
                BlockTickPolicyObservation>();
        for (BlockTickPolicyObservation observation : observations) {
            if (observation == null || byId.put(observation.id(), observation) != null) {
                throw new IllegalArgumentException("duplicate tick-policy observation");
            }
        }
        List<BlockTickPolicyEvidence> evidence = new ArrayList<BlockTickPolicyEvidence>();
        for (BlockTickPolicyScenario scenario : scenarios) {
            if (scenario == null) throw new IllegalArgumentException("null tick-policy scenario");
            BlockTickPolicyObservation observed = byId.remove(scenario.id());
            if (observed == null || observed.mechanism() != scenario.mechanism()
                    || !observed.initial().equals(scenario.initial())
                    || !observed.effect().equals(scenario.effect())
                    || observed.persisted() != scenario.persisted()) {
                throw new IllegalStateException("tick-policy observation drifted: "
                        + scenario.id());
            }
            evidence.add(new BlockTickPolicyEvidence(scenario));
        }
        if (!byId.isEmpty()) throw new IllegalArgumentException("unknown tick-policy observation");
        return Collections.unmodifiableList(evidence);
    }

    public static String canonical(List<BlockTickPolicyEvidence> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            throw new IllegalArgumentException("tick-policy evidence is empty");
        }
        StringBuilder value = new StringBuilder(
                "schema=worldline.block-tick-policy-evidence.v1\n");
        value.append("rows=").append(evidence.size()).append('\n');
        for (BlockTickPolicyEvidence row : evidence) {
            if (row == null) throw new IllegalArgumentException("null tick-policy evidence");
            value.append(row.canonical());
        }
        return value.toString();
    }
}
