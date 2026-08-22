package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Closed redstone slice: wire, torch, repeater delays, lever, button, piston
 * extend, and the power-query surface. Plates, BUD, and repeater lock stay out.
 */
final class RedstoneSemantics {
    private RedstoneSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_WIRE_TYPE",
                "net/minecraft/src/BlockRedstoneWire", "class", "BlockRedstoneWire", "-",
                "", "", "WORLD", "redstone-wire-power,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_WIRE",
                "net/minecraft/src/Block", "field", "redstoneWire", "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "redstone-wire-power,symbols.map", "aw", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_TORCH_TYPE",
                "net/minecraft/src/BlockRedstoneTorch", "class", "BlockRedstoneTorch", "-",
                "", "", "WORLD", "redstone-wire-power,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_TORCH",
                "net/minecraft/src/Block", "field", "torchRedstoneActive",
                "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "redstone-wire-power,symbols.map", "aR", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_REPEATER_TYPE",
                "net/minecraft/src/BlockRedstoneRepeater", "class", "BlockRedstoneRepeater", "-",
                "", "", "WORLD", "redstone-repeater-delay,redstone-repeater-delays,symbols.map",
                "", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_REPEATER_IDLE",
                "net/minecraft/src/Block", "field", "redstoneRepeaterIdle",
                "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "redstone-repeater-delay,redstone-repeater-delays,symbols.map",
                "bi", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_REPEATER_ACTIVE",
                "net/minecraft/src/Block", "field", "redstoneRepeaterActive",
                "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "redstone-repeater-delay,redstone-repeater-delays,symbols.map",
                "bj", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_LEVER_TYPE",
                "net/minecraft/src/BlockLever", "class", "BlockLever", "-",
                "", "", "WORLD", "redstone-lever-button,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_LEVER",
                "net/minecraft/src/Block", "field", "lever", "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "redstone-lever-button,symbols.map", "aK", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_BUTTON_TYPE",
                "net/minecraft/src/BlockButton", "class", "BlockButton", "-",
                "", "", "WORLD", "redstone-lever-button,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_BUTTON",
                "net/minecraft/src/Block", "field", "button", "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "redstone-lever-button,symbols.map", "aS", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_SCHEDULE",
                "net/minecraft/src/World", "method", "scheduleUpdateTick", "(IIIII)V",
                "WORLD", "WORLD", "CLOCK", "redstone-lever-button,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_PISTON_TYPE",
                "net/minecraft/src/BlockPistonBase", "class", "BlockPistonBase", "-",
                "", "", "WORLD", "redstone-piston-extend,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_PISTON",
                "net/minecraft/src/Block", "field", "pistonBase", "Lnet/minecraft/src/Block;",
                "WORLD", "", "WORLD", "redstone-piston-extend,symbols.map", "aa", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_PISTON_HEAD",
                "net/minecraft/src/Block", "field", "pistonExtension",
                "Lnet/minecraft/src/BlockPistonExtension;",
                "WORLD", "", "WORLD", "redstone-piston-extend,symbols.map", "ab", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_PISTON_MOVING_TYPE",
                "net/minecraft/src/BlockPistonMoving", "class", "BlockPistonMoving", "-",
                "", "", "WORLD", "redstone-piston-extend,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_PISTON_MOVING",
                "net/minecraft/src/Block", "field", "pistonMoving",
                "Lnet/minecraft/src/BlockPistonMoving;",
                "WORLD", "", "WORLD", "redstone-piston-extend,symbols.map", "ad", 9998));
        mappings.add(SemanticMapping.of("redstone", "REDSTONE_PISTON_HEAD_TYPE",
                "net/minecraft/src/BlockPistonExtension", "class", "BlockPistonExtension", "-",
                "", "", "WORLD", "redstone-piston-extend,symbols.map", "", 9998));
        mappings.add(SemanticMapping.of("redstone", "BLOCK_PROVIDES_POWER",
                "net/minecraft/src/Block", "method", "canProvidePower", "()Z",
                "WORLD", "", "WORLD", "redstone-wire-power,symbols.map", "f", 9998));
        mappings.add(SemanticMapping.of("redstone", "BLOCK_POWERING_TO",
                "net/minecraft/src/Block", "method", "isPoweringTo",
                "(Lnet/minecraft/src/IBlockAccess;IIII)Z",
                "WORLD", "", "WORLD", "redstone-repeater-delay,symbols.map", "c", 9998));
        mappings.add(SemanticMapping.of("redstone", "WORLD_INDIRECT_POWER",
                "net/minecraft/src/World", "method", "isBlockIndirectlyGettingPowered", "(III)Z",
                "WORLD,REDSTONE", "", "WORLD", "redstone-wire-power,symbols.map", "s", 9998));
        return Collections.unmodifiableList(mappings);
    }
}
