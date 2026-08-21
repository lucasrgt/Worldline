package worldline.api;

import java.util.Objects;

/** Immutable accepted furnace-output extraction with its crafted-stat count. */
public final class RemoteFurnaceExtraction {
    private final int personalSlot, takeAction, storeAction, craftedCount;
    private final RemoteItemStack stack;
    private final RemoteInventoryView before, after, personalBefore, personalAfter;
    public RemoteFurnaceExtraction(int personalSlot, int takeAction, int storeAction,
            int craftedCount, RemoteItemStack stack, RemoteInventoryView before,
            RemoteInventoryView after, RemoteInventoryView personalBefore,
            RemoteInventoryView personalAfter) {
        RemoteItemStack glass = new RemoteItemStack(20, 1, 0);
        if (personalSlot < 9 || personalSlot > 44 || takeAction < 1 || takeAction > 32766
                || storeAction != takeAction + 1 || craftedCount != 1 || !glass.equals(stack)
                || before == null || after == null || personalBefore == null || personalAfter == null
                || before.windowId() < 1 || before.windowId() > 100
                || before.windowId() != after.windowId() || before.size() != 39 || after.size() != 39
                || personalBefore.windowId() != 0 || personalAfter.windowId() != 0
                || personalBefore.size() != 45 || personalAfter.size() != 45)
            throw new IllegalArgumentException("invalid furnace extraction identity");
        int combined = personalSlot - 6;
        if (!before.slot(0).empty() || !before.slot(1).empty() || before.slot(2).empty()
                || !before.slot(2).item().equals(glass) || !before.slot(combined).empty()
                || !after.slot(0).empty() || !after.slot(1).empty() || !after.slot(2).empty()
                || after.slot(combined).empty() || !after.slot(combined).item().equals(glass)
                || !personalBefore.slot(personalSlot).empty() || personalAfter.slot(personalSlot).empty()
                || !personalAfter.slot(personalSlot).item().equals(glass))
            throw new IllegalArgumentException("invalid furnace extraction state");
        for (int slot = 0; slot < 39; slot++) if (slot != 2 && slot != combined
                && !before.slot(slot).equals(after.slot(slot)))
            throw new IllegalArgumentException("furnace extraction changed unrelated combined slot");
        for (int slot = 0; slot < 45; slot++) if (slot != personalSlot
                && !personalBefore.slot(slot).equals(personalAfter.slot(slot)))
            throw new IllegalArgumentException("furnace extraction changed unrelated personal slot");
        for (int slot = 9; slot < 45; slot++) { int mapped = slot - 6;
            if (!same(before.slot(mapped), personalBefore.slot(slot))
                    || !same(after.slot(mapped), personalAfter.slot(slot)))
                throw new IllegalArgumentException("furnace extraction tail drift"); }
        this.personalSlot = personalSlot; this.takeAction = takeAction; this.storeAction = storeAction;
        this.craftedCount = craftedCount; this.stack = stack; this.before = before; this.after = after;
        this.personalBefore = personalBefore; this.personalAfter = personalAfter;
    }
    public int personalSlot() { return personalSlot; }
    public int takeAction() { return takeAction; }
    public int storeAction() { return storeAction; }
    public int craftedCount() { return craftedCount; }
    public RemoteItemStack stack() { return stack; }
    public RemoteInventoryView before() { return before; }
    public RemoteInventoryView after() { return after; }
    public RemoteInventoryView personalBefore() { return personalBefore; }
    public RemoteInventoryView personalAfter() { return personalAfter; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteFurnaceExtraction)) return false;
        RemoteFurnaceExtraction value = (RemoteFurnaceExtraction) other;
        return personalSlot == value.personalSlot && takeAction == value.takeAction
                && storeAction == value.storeAction && craftedCount == value.craftedCount
                && stack.equals(value.stack) && before.equals(value.before) && after.equals(value.after)
                && personalBefore.equals(value.personalBefore) && personalAfter.equals(value.personalAfter); }
    @Override public int hashCode() { return Objects.hash(personalSlot, takeAction, storeAction,
            craftedCount, stack, before, after, personalBefore, personalAfter); }
    private static boolean same(RemoteInventorySlot left, RemoteInventorySlot right) {
        return left.empty() ? right.empty() : !right.empty() && left.item().equals(right.item()); }
}
