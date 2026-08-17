package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Keyboard, mouse, movement, and inventory-key input symbols for b1.7.3.
 */
final class InputSemantics {
    private InputSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("input", "KEYBOARD", "org/lwjgl/input/Keyboard", "method",
                        "isKeyDown", "(I)Z", "INPUT", "", "INPUT",
                        "lab-cycle,gui-tree,controlled-client-tick", "", 9998),
                SemanticMapping.of("input", "KEYBOARD_NEXT", "org/lwjgl/input/Keyboard", "method",
                        "next", "()Z", "INPUT", "", "INPUT",
                        "lab-cycle,gui-tree", "", 9990),
                SemanticMapping.of("input", "KEYBOARD_PUSH", "org/lwjgl/input/Keyboard", "method",
                        "worldlinePush", "(IZC)V", "", "INPUT", "INPUT",
                        "lab-cycle,gui-tree", "", 9990),
                SemanticMapping.of("input", "KEYBOARD_RESET", "org/lwjgl/input/Keyboard", "method",
                        "worldlineReset", "()V", "", "INPUT", "INPUT",
                        "lab-cycle", "", 9920),
                SemanticMapping.of("input", "MOUSE", "org/lwjgl/input/Mouse", "method",
                        "getEventX", "()I", "INPUT", "", "INPUT",
                        "lab-cycle,controlled-client-tick", "", 9920),
                SemanticMapping.of("input", "MOUSE_BUTTON", "org/lwjgl/input/Mouse", "method",
                        "isButtonDown", "(I)Z", "INPUT", "", "INPUT",
                        "lab-cycle", "", 9920),
                SemanticMapping.of("input", "MOUSE_PUSH", "org/lwjgl/input/Mouse", "method",
                        "worldlinePush", "(IZIII)V", "", "INPUT", "INPUT",
                        "lab-cycle", "", 9920),
                SemanticMapping.of("input", "MOUSE_RESET", "org/lwjgl/input/Mouse", "method",
                        "worldlineReset", "()V", "", "INPUT", "INPUT",
                        "lab-cycle", "", 9920),
                SemanticMapping.of("input", "MOVEMENT", "net/minecraft/src/EntityPlayerSP", "field",
                        "movementInput", "Lnet/minecraft/src/MovementInput;", "INPUT", "PLAYER",
                        "INPUT", "controlled-client-tick,symbols.map", "a", 9990),
                SemanticMapping.of("input", "INVENTORY_KEY", "worldline/b173/B173Keys", "field",
                        "INVENTORY", "I", "", "", "INPUT",
                        "gui-tree,lab-cycle", "", 9990),
                SemanticMapping.of("input", "ESCAPE_KEY", "worldline/b173/B173Keys", "field",
                        "ESCAPE", "I", "", "", "INPUT",
                        "gui-tree,lab-cycle", "", 9990)));
    }
}
