package worldline.api;

import java.util.Objects;

/** Immutable accepted single-stone retrieval from a single chest. */
public final class RemoteChestRetrieval {
    private final int chestSlot, personalSlot, takeAction, storeAction;
    private final RemoteItemStack stack;
    private final RemoteInventoryView before, after, personalBefore, personalAfter;
    public RemoteChestRetrieval(int chestSlot, int personalSlot, int takeAction, int storeAction,
            RemoteItemStack stack, RemoteInventoryView before, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter) {
        RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
        if (chestSlot < 0 || chestSlot > 26 || personalSlot < 9 || personalSlot > 44
                || takeAction < 1 || takeAction > 32766 || storeAction != takeAction + 1
                || !stone.equals(stack) || before == null || after == null
                || personalBefore == null || personalAfter == null || before.windowId() < 1
                || before.windowId() > 100 || before.windowId() != after.windowId()
                || before.size() != 63 || after.size() != 63
                || personalBefore.windowId() != 0 || personalAfter.windowId() != 0
                || personalBefore.size() != 45 || personalAfter.size() != 45)
            throw new IllegalArgumentException("invalid chest retrieval identity");
        int combined = personalSlot + 18;
        if (before.slot(chestSlot).empty() || !before.slot(chestSlot).item().equals(stone)
                || !before.slot(combined).empty() || !after.slot(chestSlot).empty()
                || after.slot(combined).empty() || !after.slot(combined).item().equals(stone)
                || !personalBefore.slot(personalSlot).empty() || personalAfter.slot(personalSlot).empty()
                || !personalAfter.slot(personalSlot).item().equals(stone))
            throw new IllegalArgumentException("invalid chest retrieval state");
        for (int slot = 0; slot < 63; slot++) if (slot != chestSlot && slot != combined
                && !before.slot(slot).equals(after.slot(slot)))
            throw new IllegalArgumentException("chest retrieval changed unrelated combined slot");
        for (int slot = 0; slot < 45; slot++) if (slot != personalSlot
                && !personalBefore.slot(slot).equals(personalAfter.slot(slot)))
            throw new IllegalArgumentException("chest retrieval changed unrelated personal slot");
        for (int slot = 9; slot < 45; slot++) { int mapped = slot + 18;
            if (!same(before.slot(mapped), personalBefore.slot(slot))
                    || !same(after.slot(mapped), personalAfter.slot(slot)))
                throw new IllegalArgumentException("chest retrieval tail drift"); }
        this.chestSlot = chestSlot; this.personalSlot = personalSlot; this.takeAction = takeAction;
        this.storeAction = storeAction; this.stack = stack; this.before = before; this.after = after;
        this.personalBefore = personalBefore; this.personalAfter = personalAfter;
    }
    public int chestSlot() { return chestSlot; }
    public int personalSlot() { return personalSlot; }
    public int takeAction() { return takeAction; }
    public int storeAction() { return storeAction; }
    public RemoteItemStack stack() { return stack; }
    public RemoteInventoryView before() { return before; }
    public RemoteInventoryView after() { return after; }
    public RemoteInventoryView personalBefore() { return personalBefore; }
    public RemoteInventoryView personalAfter() { return personalAfter; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteChestRetrieval)) return false;
        RemoteChestRetrieval value = (RemoteChestRetrieval) other;
        return chestSlot == value.chestSlot && personalSlot == value.personalSlot
                && takeAction == value.takeAction && storeAction == value.storeAction
                && stack.equals(value.stack) && before.equals(value.before) && after.equals(value.after)
                && personalBefore.equals(value.personalBefore) && personalAfter.equals(value.personalAfter); }
    @Override public int hashCode() { return Objects.hash(chestSlot, personalSlot, takeAction,
            storeAction, stack, before, after, personalBefore, personalAfter); }
    private static boolean same(RemoteInventorySlot left, RemoteInventorySlot right) {
        return left.empty() ? right.empty() : !right.empty() && left.item().equals(right.item()); }
}
