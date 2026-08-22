package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import worldline.api.GameUi;
import worldline.api.GameUiBounds;
import worldline.api.GameUiNode;
import worldline.api.GameUiCapability;
import worldline.api.GameUiInput;
import worldline.api.GameUiKey;
import worldline.api.GameUiLayout;
import worldline.api.GameUiImage;
import worldline.api.GameUiVisual;

/** Semantic inventory tree over the controlled client screen. */
public final class B173Gui implements GameUiInput, GameUiLayout, GameUiVisual {
    private final B173ClientBackend backend;
    private final B173GuiDriver driver;

    B173Gui(B173ClientBackend backend) { this.backend = backend; driver = new B173GuiDriver(this, backend); }

    @Override public Set<GameUiCapability> capabilities() {
        GameUi foreign = B173ForeignUi.bind(current());
        return foreign == null ? Collections.unmodifiableSet(EnumSet.of(GameUiCapability.SEMANTIC_TREE,
                GameUiCapability.INVENTORY_LIFECYCLE, GameUiCapability.NODE_CLICK,
                GameUiCapability.POINTER, GameUiCapability.DRAG_DROP, GameUiCapability.GEOMETRY))
                : foreign.capabilities();
    }

    @Override public void openInventory() { tap(B173Keys.INVENTORY); }

    @Override public void close() { if (!screen().isEmpty()) tap(B173Keys.ESCAPE); }

    public void closeScreen() { close(); }

    public void open(GuiScreen screen) {
        if (screen == null) throw new NullPointerException("screen");
        backend.client().displayGuiScreen(screen);
    }

    public void putMain(int index, int itemId, int count) {
        if (index < 0 || index >= 36) throw new IllegalArgumentException("main slot");
        if (count < 0) throw new IllegalArgumentException("count");
        ItemStack stack = itemId < 0 || count == 0 ? null : new ItemStack(itemId, count, 0);
        backend.client().thePlayer.inventory.mainInventory[index] = stack;
    }

    @Override public String screen() {
        GuiScreen screen = current();
        if (screen == null) return "";
        GameUi foreign = B173ForeignUi.bind(screen);
        if (foreign != null) return foreign.screen();
        if (screen instanceof GuiInventory) return GameUiNode.INVENTORY;
        throw new IllegalStateException("unsupported screen " + screen.getClass().getSimpleName());
    }

    @Override public List<GameUiNode> nodes() {
        GameUi foreign = B173ForeignUi.bind(current());
        if (foreign != null) return foreign.nodes();
        String screen = screen();
        if (screen.isEmpty()) return Collections.emptyList();
        List<GameUiNode> nodes = new ArrayList<GameUiNode>();
        nodes.add(new GameUiNode(GameUiNode.SCREEN, screen, -1, -1, 0));
        for (int index = 0; index < slotCount(); index++) nodes.add(slot(index));
        return Collections.unmodifiableList(nodes);
    }

    public String screenName() {
        GuiScreen screen = current();
        return screen == null ? "" : screen.getClass().getSimpleName();
    }

    public B173Gui screen(String name) {
        if (!screenName().equals(name)) {
            throw new IllegalStateException("expected screen " + name + " but found " + screenName());
        }
        return this;
    }

    @Override public GameUiNode node(String role, String name) {
        for (GameUiNode node : nodes()) {
            if (node.role().equals(role) && node.name().equals(name)) return node;
        }
        throw new IllegalStateException("no UI node " + role + "/" + name);
    }

    public int slotCount() { return container().inventorySlots.slots.size(); }

    @Override public GameUiNode slot(int index) {
        GameUi foreign = B173ForeignUi.bind(current());
        if (foreign != null) return foreign.slot(index);
        if (index < 0 || index >= slotCount()) throw new IllegalArgumentException("slot index out of range: " + index);
        Slot slot = (Slot) container().inventorySlots.slots.get(index);
        ItemStack stack = slot.getStack();
        return new GameUiNode(GameUiNode.SLOT, Integer.toString(index), index,
                stack == null ? -1 : stack.itemID, stack == null ? 0 : stack.stackSize);
    }

    @Override public void click(GameUiNode node) {
        if (node == null) throw new NullPointerException("node");
        GameUi foreign = B173ForeignUi.bind(current());
        if (foreign != null) { foreign.click(node); return; }
        if (!GameUiNode.SLOT.equals(node.role())) throw new IllegalArgumentException("only slots can be clicked");
        clickSlot(node.index(), 0);
    }

    @Override public void focus(GameUiNode node) { GameUiInput value = foreignInput(); if (value != null) value.focus(node); else driver.focus(node); }
    @Override public GameUiNode focused() { GameUiInput value = foreignInput(); return value == null ? driver.focused() : value.focused(); }
    @Override public void type(GameUiNode node, String text) { GameUiInput value = foreignInput(); if (value != null) value.type(node, text); else driver.type(node, text); }
    @Override public void press(GameUiKey key) { GameUiInput value = foreignInput(); if (value != null) value.press(key); else driver.press(key); }
    @Override public void hover(GameUiNode node) { GameUiInput value = foreignInput(); if (value != null) value.hover(node); else driver.hover(node); }
    @Override public void click(int x, int y, int button) { GameUiInput value = foreignInput(); if (value != null) value.click(x, y, button); else driver.click(x, y, button); }
    @Override public void drag(GameUiNode source, GameUiNode target, int button) {
        GameUiInput foreign = foreignInput();
        if (foreign != null) foreign.drag(source, target, button); else driver.drag(source, target, button);
    }
    @Override public GameUiBounds viewport() { GameUiLayout value = foreignLayout(); return value == null ? driver.viewport() : value.viewport(); }
    @Override public GameUiBounds bounds(GameUiNode node) { GameUiLayout value = foreignLayout(); return value == null ? driver.bounds(node) : value.bounds(node); }
    @Override public GameUiImage screenshot() {
        GameUi foreign = B173ForeignUi.bind(current());
        if (!(foreign instanceof GameUiVisual)) throw new IllegalStateException(
                "E2302 UI capability unavailable: " + GameUiCapability.SCREENSHOT);
        return ((GameUiVisual) foreign).screenshot();
    }

    public void clickSlot(int index, int button) {
        slot(index);
        GuiContainer screen = container();
        backend.client().playerController.func_27174_a(
                screen.inventorySlots.windowId, index, button, false, backend.client().thePlayer);
    }

    private void tap(int key) { backend.key(key, true, (char) 0); backend.key(key, false, (char) 0); }

    GuiScreen current() { return backend.client().currentScreen; }

    private GameUiInput foreignInput() {
        GameUi value = B173ForeignUi.bind(current()); return value instanceof GameUiInput ? (GameUiInput) value : null;
    }

    private GameUiLayout foreignLayout() {
        GameUi value = B173ForeignUi.bind(current()); return value instanceof GameUiLayout ? (GameUiLayout) value : null;
    }

    GuiContainer container() {
        GuiScreen screen = current();
        if (!(screen instanceof GuiContainer)) throw new IllegalStateException("current screen has no inventory slots");
        return (GuiContainer) screen;
    }
}
