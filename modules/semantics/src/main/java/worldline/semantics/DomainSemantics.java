package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Neutral M3 domain API handles used by Worldline automation.
 */
final class DomainSemantics {
    private DomainSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("domain", "WORLD_API", "worldline/api/GameWorld", "class",
                        "GameWorld", "-", "", "", "WORLD", "m3-domain-api", "", 9998),
                SemanticMapping.of("domain", "PLAYER_API", "worldline/api/GamePlayer", "class",
                        "GamePlayer", "-", "", "", "PLAYER", "m3-domain-api", "", 9998),
                SemanticMapping.of("domain", "ENTITY_API", "worldline/api/GameEntity", "class",
                        "GameEntity", "-", "", "", "ENTITY", "m3-domain-api", "", 9998),
                SemanticMapping.of("domain", "BLOCK_STATE", "worldline/api/BlockState", "class",
                        "BlockState", "-", "", "", "WORLD", "m3-domain-api", "", 9998),
                SemanticMapping.of("domain", "READ_BLOCK", "worldline/api/GameWorld", "method",
                        "block", "(Lworldline/api/BlockPosition;)Lworldline/api/BlockState;",
                        "WORLD", "", "WORLD", "m3-domain-api", "", 9998),
                SemanticMapping.of("domain", "WRITE_BLOCK", "worldline/api/GameWorld", "method",
                        "setBlock",
                        "(Lworldline/api/BlockPosition;Lworldline/api/BlockState;)Z",
                        "WORLD", "WORLD", "WORLD", "m3-domain-api", "", 9998),
                SemanticMapping.of("domain", "LIST_ENTITIES", "worldline/api/GameWorld",
                        "method", "entities", "()Ljava/util/List;", "WORLD", "", "ENTITY",
                        "m3-domain-api", "", 9998),
                SemanticMapping.of("domain", "TELEPORT", "worldline/api/GameEntity", "method",
                        "teleport", "(Lworldline/api/GamePosition;)V", "", "ENTITY",
                        "ENTITY", "m3-domain-api", "", 9998)));
    }
}
