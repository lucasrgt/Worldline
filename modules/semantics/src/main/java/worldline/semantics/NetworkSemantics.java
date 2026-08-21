package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Network-category mappings for the b1.7.3 semantic catalog. Roles cover the
 * offline session type, play-channel packets, and oracled protocol-14 GUI wire.
 */
final class NetworkSemantics {
    private NetworkSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("network", "OFFLINE_SESSION",
                "net/minecraft/src/Session", "class", "Session", "-",
                "", "", "NETWORK", "controlled-client-tick,lab-cycle,gui-tree", "gr", 9990));
        mappings.add(SemanticMapping.of("network", "NETWORK_DISABLED",
                "worldline/b173/B173Runtime", "method", "networkConnected", "()Z",
                "", "", "NETWORK", "lab-cycle", "", 9990));
        mappings.add(SemanticMapping.of("network", "PACKET10_FLYING",
                "net/minecraft/src/Packet10Flying", "class", "Packet10Flying", "-",
                "NETWORK", "PLAYER", "NETWORK",
                "m32-remote-terrain-render,m34-pose-correction,mappings.tiny", "ig", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET12_PLAYER_LOOK",
                "net/minecraft/src/Packet12PlayerLook", "class", "Packet12PlayerLook", "-",
                "NETWORK", "PLAYER", "NETWORK",
                "m24-play-pose,mappings.tiny", "vh", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET13_PLAYER_LOOK_MOVE",
                "net/minecraft/src/Packet13PlayerLookMove", "class", "Packet13PlayerLookMove", "-",
                "NETWORK", "PLAYER", "NETWORK",
                "m24-play-pose,m34-pose-correction,m35-movement-outcome,mappings.tiny",
                "ev", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET_STANCE",
                "net/minecraft/src/Packet10Flying", "field", "stance", "D",
                "NETWORK", "PLAYER", "NETWORK",
                "m34-pose-correction,m35-movement-outcome,mappings.tiny", "d", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET3_CHAT",
                "net/minecraft/src/Packet3Chat", "class", "Packet3Chat", "-",
                "NETWORK", "PLAYER", "NETWORK",
                "m27-multiplayer-chat,mappings.tiny", "pe", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET14_BLOCK_DIG",
                "net/minecraft/src/Packet14BlockDig", "class", "Packet14BlockDig", "-",
                "NETWORK", "WORLD", "NETWORK,WORLD",
                "m31-incremental-world,m32-remote-terrain-render,mappings.tiny", "jv", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET50_PRECHUNK",
                "net/minecraft/src/Packet50PreChunk", "class", "Packet50PreChunk", "-",
                "NETWORK", "CHUNK", "NETWORK,CHUNK",
                "m30-remote-world-cache,m33-chunk-traversal,mappings.tiny", "se", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET51_MAP_CHUNK",
                "net/minecraft/src/Packet51MapChunk", "class", "Packet51MapChunk", "-",
                "NETWORK", "CHUNK", "NETWORK,CHUNK",
                "m28-remote-chunk,m29-remote-chunk-snapshot,mappings.tiny", "ef", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET52_MULTI_BLOCK_CHANGE",
                "net/minecraft/src/Packet52MultiBlockChange", "class",
                "Packet52MultiBlockChange", "-", "NETWORK", "WORLD", "NETWORK,WORLD",
                "m31-incremental-world,mappings.tiny", "wu", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET53_BLOCK_CHANGE",
                "net/minecraft/src/Packet53BlockChange", "class", "Packet53BlockChange", "-",
                "NETWORK", "WORLD", "NETWORK,WORLD",
                "m31-incremental-world,m32-remote-terrain-render,mappings.tiny", "tv", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET5_PLAYER_INVENTORY",
                "net/minecraft/src/Packet5PlayerInventory", "class", "Packet5PlayerInventory", "-",
                "NETWORK", "PLAYER", "NETWORK,PLAYER",
                "m49-held-item-peer,m65-peer-armor,mappings.tiny", "s", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET7_USE_ENTITY",
                "net/minecraft/src/Packet7UseEntity", "class", "Packet7UseEntity", "-",
                "NETWORK", "PLAYER", "NETWORK,PLAYER",
                "m66-player-combat,mappings.tiny", "a", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET8_UPDATE_HEALTH",
                "net/minecraft/src/Packet8UpdateHealth", "class", "Packet8UpdateHealth", "-",
                "NETWORK", "PLAYER", "NETWORK,PLAYER",
                "m66-player-combat,mappings.tiny", "eu", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET15_PLACE",
                "net/minecraft/src/Packet15Place", "class", "Packet15Place", "-",
                "NETWORK", "WORLD", "NETWORK,WORLD",
                "m53-held-block-placement,m54-chest-window,mappings.tiny", "gx", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET16_BLOCK_ITEM_SWITCH",
                "net/minecraft/src/Packet16BlockItemSwitch", "class", "Packet16BlockItemSwitch",
                "-", "NETWORK", "PLAYER", "NETWORK,PLAYER",
                "m49-held-item-peer,mappings.tiny", "ho", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET21_PICKUP_SPAWN",
                "net/minecraft/src/Packet21PickupSpawn", "class", "Packet21PickupSpawn", "-",
                "NETWORK", "ENTITY", "NETWORK,ENTITY",
                "m51-dropped-item-spawn,m52-item-collection,mappings.tiny", "nd", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET22_COLLECT",
                "net/minecraft/src/Packet22Collect", "class", "Packet22Collect", "-",
                "NETWORK", "ENTITY", "NETWORK,ENTITY",
                "m52-item-collection,mappings.tiny", "di", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET29_DESTROY_ENTITY",
                "net/minecraft/src/Packet29DestroyEntity", "class", "Packet29DestroyEntity", "-",
                "NETWORK", "ENTITY", "NETWORK,ENTITY",
                "m52-item-collection,mappings.tiny", "rv", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET38_ENTITY_STATUS",
                "net/minecraft/src/Packet38EntityStatus", "class", "Packet38EntityStatus", "-",
                "NETWORK", "ENTITY", "NETWORK,PLAYER",
                "m66-player-combat,mappings.tiny", "jf", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET100_OPEN_WINDOW",
                "net/minecraft/src/Packet100OpenWindow", "class", "Packet100OpenWindow", "-",
                "NETWORK", "GUI", "NETWORK,GUI",
                "m54-chest-window,m62-workbench-window,mappings.tiny", "iw", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET101_CLOSE_WINDOW",
                "net/minecraft/src/Packet101CloseWindow", "class", "Packet101CloseWindow", "-",
                "NETWORK", "GUI", "NETWORK,GUI",
                "m58-window-lifecycle,mappings.tiny", "mn", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET102_WINDOW_CLICK",
                "net/minecraft/src/Packet102WindowClick", "class", "Packet102WindowClick", "-",
                "GUI", "NETWORK", "NETWORK,GUI",
                "m55-accepted-personal-transaction,mappings.tiny", "qs", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET103_SET_SLOT",
                "net/minecraft/src/Packet103SetSlot", "class", "Packet103SetSlot", "-",
                "NETWORK", "INVENTORY", "NETWORK,INVENTORY",
                "m48-inventory-observation,mappings.tiny", "hq", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET104_WINDOW_ITEMS",
                "net/minecraft/src/Packet104WindowItems", "class", "Packet104WindowItems", "-",
                "NETWORK", "INVENTORY", "NETWORK,INVENTORY",
                "m48-inventory-observation,m54-chest-window,mappings.tiny", "kb", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET105_UPDATE_PROGRESSBAR",
                "net/minecraft/src/Packet105UpdateProgressbar", "class",
                "Packet105UpdateProgressbar", "-", "NETWORK", "GUI", "NETWORK,GUI",
                "m60-furnace-smelt,mappings.tiny", "mv", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET106_TRANSACTION",
                "net/minecraft/src/Packet106Transaction", "class", "Packet106Transaction", "-",
                "NETWORK", "GUI", "NETWORK,GUI",
                "m55-accepted-personal-transaction,m56-rejected-transaction-recovery,mappings.tiny",
                "oj", 9998));
        mappings.add(SemanticMapping.of("network", "PACKET200_STATISTIC",
                "net/minecraft/src/Packet200Statistic", "class", "Packet200Statistic", "-",
                "NETWORK", "PLAYER", "NETWORK,GUI",
                "m61-furnace-output,m64-workbench-output,mappings.tiny", "of", 9998));
        return Collections.unmodifiableList(mappings);
    }
}
