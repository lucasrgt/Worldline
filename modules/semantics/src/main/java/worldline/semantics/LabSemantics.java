package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Lab-category mappings for the b1.7.3 semantic catalog. Roles cover
 * observation, snapshot, checkpoint, hypothesis, and comparison.
 */
final class LabSemantics {
    private LabSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("lab", "OBSERVATION",
                        "worldline/b173/B173Observation", "class", "B173Observation", "-",
                        "WORLD,PLAYER,RENDER", "", "LAB",
                        "lab-cycle,controlled-client-tick", "", 9990),
                SemanticMapping.of("lab", "SNAPSHOT",
                        "worldline/api/RuntimeSnapshot", "class", "RuntimeSnapshot", "-",
                        "", "LAB", "LAB", "m4-durable-snapshot", "", 9998),
                SemanticMapping.of("lab", "CHECKPOINT",
                        "worldline/b173/B173Checkpoint", "class", "B173Checkpoint", "-",
                        "LAB", "LAB", "LAB", "lab-cycle", "", 9990),
                SemanticMapping.of("lab", "HYPOTHESIS",
                        "worldline/b173/B173Hypothesis", "class", "B173Hypothesis", "-",
                        "LAB", "LAB", "LAB", "lab-cycle", "", 9920),
                SemanticMapping.of("lab", "COMPARISON",
                        "worldline/b173/B173Comparison", "class", "B173Comparison", "-",
                        "LAB", "LAB", "LAB", "lab-cycle", "", 9920)));
    }
}
