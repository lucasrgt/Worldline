package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public TestKit boundary for deterministic Beta 1.7.3 mob pathfinding. */
final class MobAiSemantics {
    private MobAiSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("mob-ai", "MOB_AI_PATHFINDING_SCENARIO_TESTKIT",
                        "worldline/testkit/PathfindingMatrixScenario", "method", "observe",
                        "()Lworldline/testkit/PathfindingMatrixObservation;",
                        "MOB_AI", "MOB_AI", "MOB_AI",
                        "m622-pathfinding-matrix", "", 9998),
                SemanticMapping.of("mob-ai", "MOB_AI_PATHFINDING_OBSERVATION_TESTKIT",
                        "worldline/testkit/PathfindingMatrixObservation", "method", "open",
                        "()Lworldline/testkit/PathfindingRouteObservation;",
                        "MOB_AI", "MOB_AI", "MOB_AI",
                        "m622-pathfinding-matrix", "", 9998),
                SemanticMapping.of("mob-ai", "MOB_AI_PATHFINDING_EVIDENCE_TESTKIT",
                        "worldline/testkit/PathfindingMatrixEvidence", "method", "canonical",
                        "()Ljava/lang/String;",
                        "MOB_AI", "MOB_AI", "MOB_AI",
                        "m622-pathfinding-matrix", "", 9998),
                SemanticMapping.of("mob-ai", "MOB_AI_PATHFINDING_FIXTURE_TESTKIT",
                        "worldline/testkit/PathfindingMatrixFixture", "method", "execute",
                        "(Lworldline/testkit/PathfindingMatrixScenario;)"
                                + "Lworldline/testkit/PathfindingMatrixEvidence;",
                        "MOB_AI", "MOB_AI", "MOB_AI",
                        "m622-pathfinding-matrix", "", 9998)));
    }
}
