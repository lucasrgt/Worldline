package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * RNG-category mappings for the b1.7.3 semantic catalog. Roles cover world
 * and entity random fields plus the controlled seed entry point.
 */
final class RngSemantics {
    private RngSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("rng", "WORLD_RANDOM",
                "net/minecraft/src/World", "field", "rand", "Ljava/util/Random;",
                "RNG", "RNG", "RNG",
                "lab-cycle,controlled-client-tick,deterministic-world-tick", "r", 9998));
        mappings.add(SemanticMapping.of("rng", "ENTITY_RANDOM",
                "net/minecraft/src/Entity", "field", "rand", "Ljava/util/Random;",
                "RNG", "RNG", "RNG", "lab-cycle,controlled-client-tick", "", 9990));
        mappings.add(SemanticMapping.of("rng", "CONTROLLED_SEED",
                "worldline/b173/B173ClientBackend", "method", "reseed", "(J)V",
                "", "RNG", "RNG", "lab-cycle", "", 9920));
        return Collections.unmodifiableList(mappings);
    }
}
