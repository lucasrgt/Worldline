package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Block type, numeric id, and stone/bedrock/sand constants.
 */
final class BlockSemantics {
    private BlockSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("block", "BLOCK_TYPE",
                "net/minecraft/src/Block", "class", "Block", "-",
                "", "", "WORLD", "controlled-client-tick,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("block", "BLOCK_ID",
                "net/minecraft/src/Block", "field", "blockID", "I",
                "WORLD", "", "WORLD", "controlled-client-tick,symbols.map", "bn", 9998));
        mappings.add(SemanticMapping.of("block", "BLOCK_STONE",
                "net/minecraft/src/Block", "field", "stone", "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "controlled-client-tick,symbols.map", "u", 9998));
        mappings.add(SemanticMapping.of("block", "BLOCK_BEDROCK",
                "net/minecraft/src/Block", "field", "bedrock", "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "controlled-client-tick,symbols.map", "A", 9998));
        mappings.add(SemanticMapping.of("block", "BLOCK_SAND",
                "net/minecraft/src/Block", "field", "sand", "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "deterministic-world-tick,symbols.map", "F", 9998));
        mappings.add(SemanticMapping.of("block", "BLOCK_SAND_TYPE",
                "net/minecraft/src/BlockSand", "class", "BlockSand", "-",
                "", "", "WORLD", "deterministic-world-tick,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("block", "BLOCK_SAND_FALL",
                "net/minecraft/src/BlockSand", "field", "fallInstantly", "Z",
                "WORLD", "WORLD", "WORLD", "deterministic-world-tick,symbols.map", "a", 9998));
        return Collections.unmodifiableList(mappings);
    }
}
