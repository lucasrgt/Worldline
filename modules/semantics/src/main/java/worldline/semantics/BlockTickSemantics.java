package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public TestKit boundary for scheduled, random, and tile-entity block ticks. */
final class BlockTickSemantics {
    private static final String EVIDENCE =
            "m305-plant-growth,m342-gravity-block-set,m577-wheat-light-halt-set";

    private BlockTickSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("block-tick", "BLOCK_TICK_POLICY_MECHANISM",
                        "worldline/testkit/BlockTickPolicyMechanism", "class",
                        "BlockTickPolicyMechanism", "-", "", "", "BLOCK_TICK",
                        EVIDENCE, "", 9998),
                SemanticMapping.of("block-tick", "BLOCK_TICK_POLICY_SCENARIO",
                        "worldline/testkit/BlockTickPolicyScenario", "class",
                        "BlockTickPolicyScenario", "-", "", "BLOCK_TICK", "BLOCK_TICK",
                        EVIDENCE, "", 9998),
                SemanticMapping.of("block-tick", "BLOCK_TICK_POLICY_OBSERVATION",
                        "worldline/testkit/BlockTickPolicyObservation", "class",
                        "BlockTickPolicyObservation", "-", "BLOCK_TICK", "", "BLOCK_TICK",
                        EVIDENCE, "", 9998),
                SemanticMapping.of("block-tick", "BLOCK_TICK_POLICY_FIXTURE",
                        "worldline/testkit/BlockTickPolicyFixture", "method", "execute",
                        "(Ljava/util/List;Ljava/util/List;)Ljava/util/List;", "BLOCK_TICK",
                        "BLOCK_TICK", "BLOCK_TICK", EVIDENCE, "", 9998),
                SemanticMapping.of("block-tick", "BLOCK_TICK_POLICY_EVIDENCE",
                        "worldline/testkit/BlockTickPolicyEvidence", "class",
                        "BlockTickPolicyEvidence", "-", "BLOCK_TICK", "", "BLOCK_TICK",
                        EVIDENCE, "", 9998)));
    }
}
