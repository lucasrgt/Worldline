package worldline.api;

import java.util.Objects;

/** Immutable two-click player-to-chest accepted transfer. */
public final class RemoteChestTransfer {
    private final int personalSlot, chestSlot, takeAction, storeAction;
    private final RemoteItemStack stack;
    private final RemoteInventoryView before, after;

    public RemoteChestTransfer(int personalSlot, int chestSlot, int takeAction, int storeAction,
            RemoteItemStack stack, RemoteInventoryView before, RemoteInventoryView after) {
        if (personalSlot < 9 || personalSlot > 44 || chestSlot < 0 || chestSlot > 26
                || takeAction < 1 || takeAction > 32766 || storeAction != takeAction + 1
                || stack == null || before == null || after == null || before.windowId() < 1
                || before.windowId() > 100 || before.windowId() != after.windowId()
                || before.size() != 63 || after.size() != 63)
            throw new IllegalArgumentException("invalid chest transfer identity");
        int combined = personalSlot + 18;
        if (before.slot(combined).empty() || !before.slot(combined).item().equals(stack)
                || !before.slot(chestSlot).empty() || !after.slot(combined).empty()
                || after.slot(chestSlot).empty() || !after.slot(chestSlot).item().equals(stack))
            throw new IllegalArgumentException("invalid chest transfer state");
        for (int slot = 0; slot < 63; slot++) if (slot != combined && slot != chestSlot
                && !before.slot(slot).equals(after.slot(slot)))
            throw new IllegalArgumentException("chest transfer changed unrelated slot");
        this.personalSlot = personalSlot; this.chestSlot = chestSlot;
        this.takeAction = takeAction; this.storeAction = storeAction; this.stack = stack;
        this.before = before; this.after = after;
    }
    public int personalSlot() { return personalSlot; }
    public int chestSlot() { return chestSlot; }
    public int takeAction() { return takeAction; }
    public int storeAction() { return storeAction; }
    public RemoteItemStack stack() { return stack; }
    public RemoteInventoryView before() { return before; }
    public RemoteInventoryView after() { return after; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteChestTransfer)) return false; RemoteChestTransfer value = (RemoteChestTransfer) other;
        return personalSlot == value.personalSlot && chestSlot == value.chestSlot
                && takeAction == value.takeAction && storeAction == value.storeAction
                && stack.equals(value.stack) && before.equals(value.before) && after.equals(value.after); }
    @Override public int hashCode() {
        return Objects.hash(personalSlot, chestSlot, takeAction, storeAction, stack, before, after); }
}
