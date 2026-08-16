package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import worldline.api.GameUi;
import worldline.api.GameUiNode;

/** Semantic inventory tree over the controlled client screen. */
public final class B173Gui implements GameUi {
    private final B173ClientBackend backend;

    B173Gui(B173ClientBackend backend) { this.backend = backend; }

    @Override public void openInventory() { tap(B173Keys.INVENTORY); }

    @Override public void close() { if (!screen().isEmpty()) tap(B173Keys.ESCAPE); }

    public void closeScreen() { close(); }

    @Override public String screen() {
        GuiScreen screen = current();
        if (screen == null) return "";
        if (screen instanceof GuiInventory) return GameUiNode.INVENTORY;
        throw new IllegalStateException("unsupported screen " + screen.getClass().getSimpleName());
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

    @Override public List<GameUiNode> nodes() {
        String screen = screen();
        if (screen.isEmpty()) return Collections.emptyList();
        List<GameUiNode> nodes = new ArrayList<GameUiNode>();
        nodes.add(new GameUiNode(GameUiNode.SCREEN, screen, -1, -1, 0));
        for (int index = 0; index < slotCount(); index++) nodes.add(slot(index));
        return Collections.unmodifiableList(nodes);
    }

    @Override public GameUiNode node(String role, String name) {
        for (GameUiNode node : nodes()) {
            if (node.role().equals(role) && node.name().equals(name)) return node;
        }
        throw new IllegalStateException("no UI node " + role + "/" + name);
    }

    public int slotCount() { return container().inventorySlots.slots.size(); }

    @Override public GameUiNode slot(int index) {
        if (index < 0 || index >= slotCount()) throw new IllegalArgumentException("slot index out of range: " + index);
        Slot slot = (Slot) container().inventorySlots.slots.get(index);
        ItemStack stack = slot.getStack();
        return new GameUiNode(GameUiNode.SLOT, Integer.toString(index), index,
                stack == null ? -1 : stack.itemID, stack == null ? 0 : stack.stackSize);
    }

    @Override public void click(GameUiNode node) {
        if (node == null) throw new NullPointerException("node");
        if (!GameUiNode.SLOT.equals(node.role())) throw new IllegalArgumentException("only slots can be clicked");
        clickSlot(node.index(), 0);
    }

    public void clickSlot(int index, int button) {
        slot(index);
        GuiContainer screen = container();
        backend.client().playerController.func_27174_a(
                screen.inventorySlots.windowId, index, button, false, backend.client().thePlayer);
    }

    private void tap(int key) { backend.key(key, true, (char) 0); backend.key(key, false, (char) 0); }

    private GuiScreen current() { return backend.client().currentScreen; }

    private GuiContainer container() {
        GuiScreen screen = current();
        if (!(screen instanceof GuiContainer)) throw new IllegalStateException("current screen has no inventory slots");
        return (GuiContainer) screen;
    }
}
