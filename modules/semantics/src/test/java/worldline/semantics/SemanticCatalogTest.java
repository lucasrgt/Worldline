package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import worldline.api.SemanticMapping;

public final class SemanticCatalogTest {
    private SemanticCatalogTest() {}

    public static void main(String[] arguments) {
        standardCatalogIsComplete();
        everyCategoryHasExactRoles();
        lookupFailsClosed();
        incompleteCatalogFails();
        symbolMapsAreCovered();
        traceFieldsAndStepsResolve();
        System.out.println("SemanticCatalogTest passed");
    }

    private static void standardCatalogIsComplete() {
        SemanticCatalog first = SemanticCatalog.standard();
        SemanticCatalog second = SemanticCatalog.standard();
        require(first.size() == SemanticRoles.roleCount(), "role count");
        require(first.categories().size() == 27, "category count");
        require("k".equals(first.role("CLIENT_TICK_ROOT").official()), "official alias");
        require(first.role("CLIENT_CLOCK_SOURCE").official().isEmpty(), "worldline-only alias");
        require("u".equals(first.role("BLOCK_STONE").official()), "stone alias");
        require("aN".equals(first.role("ENTITY_POS_Y").official()), "posY alias");
        require("F".equals(first.role("BLOCK_SAND").official()), "sand alias");
        require("a".equals(first.role("BLOCK_ID_READ").official()), "getBlockId alias");
        require("f".equals(first.role("CLIENT_WORLD").official()), "theWorld alias");
        require("c".equals(first.role("ITEM_ID").official()), "itemID alias");
        require("r".equals(first.role("WORLD_RANDOM").official()), "world rand alias");
        require("EntityLiving".equals(first.role("LIVING_TYPE").name()), "living type");
        require("Minecraft".equals(first.role("CLIENT_TYPE").name()), "client type");
        require("StatFileWriter".equals(first.role("STAT_FILE").name()), "stat file");
        require("IBlockAccess".equals(first.role("BLOCK_ACCESS").name()), "block access");
        require("GameWorld".equals(first.role("WORLD_API").name()), "domain world");
        require("saveChunks".equals(first.role("SAVE_CHUNKS").name()), "save chunks");
        require("updateRenderers".equals(first.role("COMPILE_CHUNKS").name()), "compile chunks");
        require("autosavePeriod".equals(first.role("AUTOSAVE_PERIOD").name()), "autosave period");
        require("addVertex".equals(first.role("ADD_VERTEX").name()), "add vertex");
        require("Packet13PlayerLookMove".equals(first.role("PACKET13_PLAYER_LOOK_MOVE").name()),
                "packet13 type");
        require("stance".equals(first.role("PACKET_STANCE").name()), "packet stance");
        require("Packet50PreChunk".equals(first.role("PACKET50_PRECHUNK").name()), "packet50");
        require("Packet51MapChunk".equals(first.role("PACKET51_MAP_CHUNK").name()), "packet51");
        require("Packet52MultiBlockChange".equals(first.role("PACKET52_MULTI_BLOCK_CHANGE").name()),
                "packet52");
        require("Packet53BlockChange".equals(first.role("PACKET53_BLOCK_CHANGE").name()), "packet53");
        require("Packet3Chat".equals(first.role("PACKET3_CHAT").name()), "packet3");
        require("Packet14BlockDig".equals(first.role("PACKET14_BLOCK_DIG").name()), "packet14");
        require("Packet5PlayerInventory".equals(first.role("PACKET5_PLAYER_INVENTORY").name()),
                "packet5");
        require("Packet7UseEntity".equals(first.role("PACKET7_USE_ENTITY").name()), "packet7");
        require("Packet8UpdateHealth".equals(first.role("PACKET8_UPDATE_HEALTH").name()), "packet8");
        require("Packet15Place".equals(first.role("PACKET15_PLACE").name()), "packet15");
        require("Packet16BlockItemSwitch".equals(first.role("PACKET16_BLOCK_ITEM_SWITCH").name()),
                "packet16");
        require("Packet21PickupSpawn".equals(first.role("PACKET21_PICKUP_SPAWN").name()), "packet21");
        require("Packet22Collect".equals(first.role("PACKET22_COLLECT").name()), "packet22");
        require("Packet29DestroyEntity".equals(first.role("PACKET29_DESTROY_ENTITY").name()),
                "packet29");
        require("Packet38EntityStatus".equals(first.role("PACKET38_ENTITY_STATUS").name()),
                "packet38");
        require("Packet100OpenWindow".equals(first.role("PACKET100_OPEN_WINDOW").name()),
                "packet100");
        require("Packet101CloseWindow".equals(first.role("PACKET101_CLOSE_WINDOW").name()),
                "packet101");
        require("Packet102WindowClick".equals(first.role("PACKET102_WINDOW_CLICK").name()),
                "packet102");
        require("Packet103SetSlot".equals(first.role("PACKET103_SET_SLOT").name()), "packet103");
        require("Packet104WindowItems".equals(first.role("PACKET104_WINDOW_ITEMS").name()),
                "packet104");
        require("Packet105UpdateProgressbar".equals(
                first.role("PACKET105_UPDATE_PROGRESSBAR").name()), "packet105");
        require("Packet106Transaction".equals(first.role("PACKET106_TRANSACTION").name()),
                "packet106");
        require("Packet200Statistic".equals(first.role("PACKET200_STATISTIC").name()), "packet200");
        require("updateRenderer".equals(first.role("CHUNK_REBUILD").name()), "chunk rebuild");
        require("NibbleArray".equals(first.role("NIBBLE_ARRAY").name()), "nibble array");
        require("setNibble".equals(first.role("SET_NIBBLE").name()), "set nibble");
        require("slots".equals(first.role("CONTAINER_SLOT_LIST").name()), "container slots list");
        require("getStack".equals(first.role("SLOT_STACK").name()), "slot stack");
        require("BlockRedstoneWire".equals(first.role("REDSTONE_WIRE_TYPE").name()),
                "redstone wire type");
        require("execute".equals(first.role("BLOCK_TICK_POLICY_FIXTURE").name()),
                "block tick fixture");
        require("execute".equals(first.role("REDSTONE_PISTON_TESTKIT").name()),
                "redstone TestKit fixture");
        require("craftPersonal2x2".equals(first.role("CRAFTING_PERSONAL_GRID_TESTKIT").name()),
                "crafting personal TestKit contract");
        require("execute".equals(first.role("TILE_ENTITY_SIGN_TESTKIT").name()),
                "tile entity sign TestKit fixture");
        require(first.sha256().equals(second.sha256())
                && first.sha256().equals(
                "d5f9c1497442d0f8126d9b34843d69a8f92363d5f1823cb0502374f83b17a88d"),
                "catalog hash drifted to " + first.sha256());
        require(first.canonical().equals(second.canonical()), "catalog canonical drifted");
        require(first.render().contains("complete=true"), "render completeness");
        require(first.role("CLIENT_TICK_ROOT").name().equals("runTick"), "tick root");
    }

    private static void everyCategoryHasExactRoles() {
        SemanticCatalog catalog = SemanticCatalog.standard();
        for (String category : SemanticRoles.categories()) {
            java.util.List<String> required = SemanticRoles.required(category);
            java.util.List<SemanticMapping> found = catalog.category(category);
            require(found.size() == required.size(), "size " + category);
            for (String role : required) {
                SemanticMapping mapping = catalog.role(role);
                require(category.equals(mapping.category()), "category of " + role);
                require(mapping.known(), "unknown " + role);
                require(!mapping.evidence().isEmpty(), "evidence " + role);
            }
        }
    }

    private static void lookupFailsClosed() {
        SemanticCatalog catalog = SemanticCatalog.standard();
        failure(() -> catalog.role("NOT_A_ROLE"));
        failure(() -> catalog.category("energy"));
        failure(() -> catalog.symbol("missing/Owner", "nope"));
    }

    private static void incompleteCatalogFails() {
        SemanticMapping tick = SemanticMapping.of("tick", "CLIENT_TICK_ROOT",
                "net/minecraft/client/Minecraft", "method", "runTick", "()V",
                "INPUT", "WORLD", "CLOCK", "controlled-client-tick", 9998);
        failure(() -> SemanticCatalog.of(Collections.singletonList(tick)));
        failure(() -> SemanticCatalog.of(Arrays.asList(tick, tick)));
    }

    private static void traceFieldsAndStepsResolve() {
        require("ENTITY_POS_Y".equals(SemanticFields.role("y")), "trace y role");
        require("HOTBAR_SLOT".equals(SemanticFields.role("slot")), "trace slot role");
        require(SemanticFields.role("schema").isEmpty(), "structural field has no role");
        require(SemanticSteps.disposable("observe:target"), "observe is disposable");
        require(SemanticSteps.boundary("tap:2") && SemanticSteps.boundary("tick"), "boundary steps");
        require(!SemanticSteps.disposable("tick") && SemanticSteps.category("reseed:1").equals("rng"),
                "tick and reseed classification");
    }

    private static void symbolMapsAreCovered() {
        SemanticCatalog catalog = SemanticCatalog.standard();
        cover("smokes/controlled-client-tick/symbols.map", catalog);
        cover("smokes/deterministic-world-tick/symbols.map", catalog);
        cover("smokes/m10-native-render/symbols.map", catalog);
        cover("smokes/redstone-semantics/symbols.map", catalog);
        cover("smokes/redstone-wire-power/symbols.map", catalog);
        cover("smokes/redstone-repeater-delay/symbols.map", catalog);
        cover("smokes/redstone-repeater-delays/symbols.map", catalog);
        cover("smokes/redstone-lever-button/symbols.map", catalog);
        cover("smokes/redstone-piston-extend/symbols.map", catalog);
    }

    private static void cover(String path, SemanticCatalog catalog) {
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(
                    java.nio.file.Paths.get(path), java.nio.charset.StandardCharsets.UTF_8);
            int covered = 0;
            for (String line : lines) {
                if (line.isEmpty() || line.charAt(0) == '#') continue;
                String[] columns = line.split("\t", -1);
                require(columns.length >= 4, "map columns " + path);
                String owner = columns[0], kind = columns[1], named = columns[3];
                if ("c".equals(kind)) {
                    boolean found = false;
                    for (SemanticMapping mapping : catalog.mappings()) {
                        if (mapping.owner().equals(owner)) { found = true; break; }
                    }
                    require(found, "unmapped class " + owner);
                } else {
                    catalog.symbol(owner, named);
                }
                covered++;
            }
            require(covered > 0, "empty map " + path);
        } catch (java.io.IOException error) {
            throw new AssertionError("cannot read " + path, error);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("expected fail-closed catalog"); }
        catch (IllegalArgumentException expected) { }
    }
}
