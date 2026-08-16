package worldline.b173;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

/** Semantic GUI selectors and actions over the controlled client. */
public final class B173Gui {
    private final B173Runtime runtime;

    B173Gui(B173Runtime runtime) { this.runtime = runtime; }

    public void openInventory() { runtime.tap(B173Keys.INVENTORY); }

    public void closeScreen() { runtime.tap(B173Keys.ESCAPE); }

    public String screenName() {
        GuiScreen screen = runtime.backend().client().currentScreen;
        return screen == null ? "" : screen.getClass().getSimpleName();
    }

    public B173Gui screen(String name) {
        if (!screenName().equals(name)) {
            throw new IllegalStateException("expected screen " + name + " but found " + screenName());
        }
        return this;
    }

    public int slotCount() { return container().inventorySlots.slots.size(); }

    public B173GuiSlot slot(int index) {
        if (index < 0 || index >= slotCount()) {
            throw new IllegalArgumentException("slot index out of range: " + index);
        }
        Slot slot = (Slot) container().inventorySlots.slots.get(index);
        ItemStack stack = slot.getStack();
        return new B173GuiSlot(index, stack == null ? -1 : stack.itemID,
                stack == null ? 0 : stack.stackSize);
    }

    public void clickSlot(int index, int button) {
        slot(index);
        GuiContainer screen = container();
        runtime.backend().client().playerController.func_27174_a(
                screen.inventorySlots.windowId, index, button, false,
                runtime.backend().client().thePlayer);
    }

    private GuiContainer container() {
        GuiScreen screen = runtime.backend().client().currentScreen;
        if (!(screen instanceof GuiContainer)) {
            throw new IllegalStateException("current screen has no inventory slots");
        }
        return (GuiContainer) screen;
    }
}
