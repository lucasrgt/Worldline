package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * World type, entity lists, block access, and difficulty symbols.
 */
final class WorldSemantics {
    private WorldSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("world", "WORLD_TYPE",
                "net/minecraft/src/World", "class", "World", "-",
                "", "", "WORLD",
                "deterministic-world-tick,controlled-client-tick,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("world", "LOADED_ENTITY_LIST",
                "net/minecraft/src/World", "field", "loadedEntityList", "Ljava/util/List;",
                "ENTITY", "ENTITY", "WORLD",
                "controlled-client-tick,symbols.map,m3-domain-api", "b", 9998));
        mappings.add(SemanticMapping.of("world", "TILE_ENTITIES",
                "net/minecraft/src/World", "field", "loadedTileEntityList", "Ljava/util/List;",
                "WORLD", "WORLD", "WORLD", "lab-cycle,invariants", "", 9920));
        mappings.add(SemanticMapping.of("world", "BLOCK_ACCESS",
                "net/minecraft/src/IBlockAccess", "class", "IBlockAccess", "-",
                "", "", "WORLD",
                "controlled-client-tick,symbols.map,m3-domain-api", "", 9998));
        mappings.add(SemanticMapping.of("world", "BLOCK_ID_READ",
                "net/minecraft/src/IBlockAccess", "method", "getBlockId", "(III)I",
                "WORLD", "", "WORLD",
                "deterministic-world-tick,m3-domain-api,symbols.map", "a", 9998));
        mappings.add(SemanticMapping.of("world", "BLOCK_READ",
                "net/minecraft/src/IBlockAccess", "method", "getBlockMetadata", "(III)I",
                "WORLD", "", "WORLD",
                "controlled-client-tick,symbols.map,m3-domain-api", "e", 9998));
        mappings.add(SemanticMapping.of("world", "BLOCK_WRITE",
                "net/minecraft/src/World", "method", "setBlockAndMetadataWithNotify", "(IIIII)Z",
                "WORLD", "WORLD", "WORLD",
                "controlled-client-tick,symbols.map,m3-domain-api", "b", 9998));
        mappings.add(SemanticMapping.of("world", "BLOCK_NOTIFY",
                "net/minecraft/src/World", "method", "setBlockWithNotify", "(IIII)Z",
                "WORLD", "WORLD", "WORLD",
                "deterministic-world-tick,symbols.map", "f", 9998));
        mappings.add(SemanticMapping.of("world", "WORLD_DIFFICULTY",
                "net/minecraft/src/World", "field", "difficultySetting", "I",
                "WORLD", "WORLD", "WORLD", "lab-cycle,invariants", "", 9920));
        mappings.add(SemanticMapping.of("world", "WORLD_PROVIDER",
                "net/minecraft/src/WorldProvider", "class", "WorldProvider", "-",
                "", "", "WORLD", "deterministic-world-tick,lab-cycle", "", 9920));
        return Collections.unmodifiableList(mappings);
    }
}
