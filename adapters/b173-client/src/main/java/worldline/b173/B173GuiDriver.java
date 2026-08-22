package worldline.b173;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Slot;
import worldline.api.GameUiBounds;
import worldline.api.GameUiCapability;
import worldline.api.GameUiKey;
import worldline.api.GameUiNode;

/** Headless-safe native pointer and geometry boundary for vanilla containers. */
final class B173GuiDriver {
    private final B173Gui ui;
    private final B173ClientBackend backend;

    B173GuiDriver(B173Gui ui, B173ClientBackend backend) {
        this.ui = ui; this.backend = backend;
    }

    void focus(GameUiNode node) { throw unavailable(GameUiCapability.FOCUS); }
    GameUiNode focused() { throw unavailable(GameUiCapability.FOCUS); }
    void type(GameUiNode node, String text) { throw unavailable(GameUiCapability.TEXT_INPUT); }
    void fill(GameUiNode node, String text) { throw unavailable(GameUiCapability.TEXT_REPLACE); }
    void press(GameUiKey key) { throw unavailable(GameUiCapability.KEYBOARD); }

    void hover(GameUiNode node) {
        GameUiBounds bounds = bounds(node); pointer(bounds.centerX(), bounds.centerY(), -1, false);
    }

    void rightClick(GameUiNode node) {
        GameUiBounds bounds = bounds(node); click(bounds.centerX(), bounds.centerY(), 1);
    }

    void setValue(GameUiNode node, int value) { throw unavailable(GameUiCapability.VALUE_INPUT); }

    void click(int x, int y, int button) {
        button(button); pointer(x, y, button, true); pointer(x, y, button, false);
    }

    void drag(GameUiNode source, GameUiNode target, int button) {
        button(button); GameUiBounds from = bounds(source), to = bounds(target);
        pointer(from.centerX(), from.centerY(), button, true);
        pointer(to.centerX(), to.centerY(), -1, false);
        pointer(to.centerX(), to.centerY(), button, false);
    }

    GameUiBounds viewport() {
        ui.require(GameUiCapability.GEOMETRY); GuiScreen screen = screen();
        return new GameUiBounds(0, 0, screen.width, screen.height);
    }

    GameUiBounds bounds(GameUiNode node) {
        if (node == null) throw new NullPointerException("node");
        ui.require(GameUiCapability.GEOMETRY);
        if (GameUiNode.SCREEN.equals(node.role())) return viewport();
        if (!GameUiNode.SLOT.equals(node.role())) throw new IllegalArgumentException("no vanilla bounds for " + node);
        GuiContainer screen = ui.container(); Slot slot = slot(screen, node.index());
        int left = (screen.width - B173Reflect.getInt(GuiContainer.class, "xSize", screen)) / 2;
        int top = (screen.height - B173Reflect.getInt(GuiContainer.class, "ySize", screen)) / 2;
        return new GameUiBounds(left + slot.xDisplayPosition, top + slot.yDisplayPosition, 16, 16);
    }

    private void pointer(int x, int y, int button, boolean pressed) {
        GameUiBounds viewport = viewport();
        if (!viewport.contains(x, y)) throw new IllegalArgumentException("pointer outside viewport");
        int rawX = x * backend.client().displayWidth / viewport.width();
        int rawY = (viewport.height() - y - 1) * backend.client().displayHeight / viewport.height();
        backend.mouse(button, pressed, 0, rawX, rawY);
    }

    private GuiScreen screen() {
        GuiScreen screen = ui.current();
        if (screen == null) throw new IllegalStateException("current screen is closed");
        return screen;
    }

    private static Slot slot(GuiContainer screen, int index) {
        if (index < 0 || index >= screen.inventorySlots.slots.size()) {
            throw new IllegalArgumentException("slot index out of range: " + index);
        }
        return (Slot) screen.inventorySlots.slots.get(index);
    }

    private static void button(int button) {
        if (button < 0 || button > 2) throw new IllegalArgumentException("mouse button out of range");
    }

    private static IllegalStateException unavailable(GameUiCapability capability) {
        return new IllegalStateException("E2302 UI capability unavailable: " + capability);
    }
}
