package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Current screen, inventory container, HUD, and GameUi symbols.
 */
final class GuiSemantics {
    private GuiSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("gui", "CURRENT_SCREEN", "net/minecraft/client/Minecraft",
                        "field", "currentScreen", "Lnet/minecraft/src/GuiScreen;", "GUI", "GUI",
                        "GUI,INPUT", "gui-tree,lab-cycle,controlled-client-tick", "r", 9998),
                SemanticMapping.of("gui", "INVENTORY_SCREEN", "net/minecraft/src/GuiInventory",
                        "class", "GuiInventory", "-", "PLAYER", "GUI", "GUI",
                        "gui-tree", "ue", 9998),
                SemanticMapping.of("gui", "CONTAINER_CLICK", "net/minecraft/src/PlayerController",
                        "method", "func_27174_a", "(IIIZLnet/minecraft/src/EntityPlayer;)V",
                        "GUI", "PLAYER", "GUI",
                        "gui-tree,controlled-client-tick", "a", 9998),
                SemanticMapping.of("gui", "HUD_TYPE", "net/minecraft/src/GuiIngame", "class",
                        "GuiIngame", "-", "PLAYER", "GUI", "GUI",
                        "controlled-client-tick,symbols.map", "", 9998),
                SemanticMapping.of("gui", "HUD_TICK", "net/minecraft/src/GuiIngame", "method",
                        "updateTick", "()V", "PLAYER", "GUI", "GUI",
                        "controlled-client-tick,symbols.map", "a", 9850),
                SemanticMapping.of("gui", "HUD_COUNTER", "net/minecraft/src/GuiIngame", "field",
                        "updateCounter", "I", "GUI", "GUI", "GUI",
                        "lab-cycle,controlled-client-tick", "", 9920),
                SemanticMapping.of("gui", "GUI_OPEN", "worldline/api/GameUi", "method",
                        "openInventory", "()V", "", "GUI", "GUI,INPUT", "gui-tree", "", 9998),
                SemanticMapping.of("gui", "GUI_CLOSE", "worldline/api/GameUi", "method",
                        "close", "()V", "", "GUI", "GUI,INPUT", "gui-tree", "", 9998),
                SemanticMapping.of("gui", "GUI_SLOT", "worldline/api/GameUi", "method",
                        "slot", "(I)Lworldline/api/GameUiNode;", "GUI", "", "GUI",
                        "gui-tree", "", 9998),
                SemanticMapping.of("gui", "GUI_CLICK", "worldline/api/GameUi", "method",
                        "click", "(Lworldline/api/GameUiNode;)V", "GUI", "GUI", "GUI",
                        "gui-tree", "", 9998),
                SemanticMapping.of("gui", "GUI_SCREEN", "net/minecraft/src/GuiScreen", "class",
                        "GuiScreen", "-", "", "GUI", "GUI", "gui-tree", "", 9990),
                SemanticMapping.of("gui", "GUI_CONTAINER", "net/minecraft/src/GuiContainer",
                        "class", "GuiContainer", "-", "GUI", "GUI", "GUI",
                        "gui-tree", "id", 9990),
                SemanticMapping.of("gui", "GUI_SLOT_TYPE", "net/minecraft/src/Slot", "class",
                        "Slot", "-", "GUI", "", "GUI", "gui-tree", "gp", 9990),
                SemanticMapping.of("gui", "CONTAINER_SLOTS", "net/minecraft/src/GuiContainer",
                        "field", "inventorySlots", "Lnet/minecraft/src/Container;", "GUI", "",
                        "GUI", "gui-tree", "j", 9990),
                SemanticMapping.of("gui", "WINDOW_ID", "net/minecraft/src/Container", "field",
                        "windowId", "I", "GUI", "", "GUI", "gui-tree", "f", 9990)));
    }
}
