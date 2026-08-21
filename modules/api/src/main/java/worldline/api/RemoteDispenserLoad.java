package worldline.api;

import java.util.Objects;

/** Immutable two-click personal-tail to dispenser accepted transfer. */
public final class RemoteDispenserLoad {
    private final int personalSlot, dispenserSlot, takeAction, storeAction;
    private final RemoteItemStack stack;
    private final RemoteInventoryView before, after, personalBefore, personalAfter;

    public RemoteDispenserLoad(int personalSlot, int dispenserSlot, int takeAction, int storeAction,
            RemoteItemStack stack, RemoteInventoryView before, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter) {
        if (personalSlot < 9 || personalSlot > 44 || dispenserSlot < 0 || dispenserSlot > 8
                || takeAction < 1 || takeAction > 32766 || storeAction != takeAction + 1
                || stack == null || before == null || after == null || personalBefore == null
                || personalAfter == null || before.windowId() < 1 || before.windowId() > 100
                || before.windowId() != after.windowId() || before.size() != 45 || after.size() != 45
                || personalBefore.windowId() != 0 || personalAfter.windowId() != 0
                || personalBefore.size() != 45 || personalAfter.size() != 45
                || (stack.legacyId() != 4 && stack.legacyId() != 262) || stack.count() != 1)
            throw new IllegalArgumentException("invalid dispenser load identity");
        if (before.slot(personalSlot).empty() || !before.slot(personalSlot).item().equals(stack)
                || !before.slot(dispenserSlot).empty() || !after.slot(personalSlot).empty()
                || after.slot(dispenserSlot).empty() || !after.slot(dispenserSlot).item().equals(stack)
                || personalBefore.slot(personalSlot).empty()
                || !personalBefore.slot(personalSlot).item().equals(stack)
                || !personalAfter.slot(personalSlot).empty())
            throw new IllegalArgumentException("invalid dispenser load state");
        for (int slot = 0; slot < 45; slot++) if (slot != personalSlot && slot != dispenserSlot
                && !before.slot(slot).equals(after.slot(slot)))
            throw new IllegalArgumentException("dispenser load changed unrelated combined slot");
        for (int slot = 0; slot < 45; slot++) if (slot != personalSlot
                && !personalBefore.slot(slot).equals(personalAfter.slot(slot)))
            throw new IllegalArgumentException("dispenser load changed unrelated personal slot");
        for (int slot = 9; slot < 45; slot++)
            if (!same(before.slot(slot), personalBefore.slot(slot))
                    || !same(after.slot(slot), personalAfter.slot(slot)))
                throw new IllegalArgumentException("dispenser tail drift");
        this.personalSlot = personalSlot; this.dispenserSlot = dispenserSlot;
        this.takeAction = takeAction; this.storeAction = storeAction; this.stack = stack;
        this.before = before; this.after = after;
        this.personalBefore = personalBefore; this.personalAfter = personalAfter;
    }

    public int personalSlot() { return personalSlot; }
    public int dispenserSlot() { return dispenserSlot; }
    public int takeAction() { return takeAction; }
    public int storeAction() { return storeAction; }
    public RemoteItemStack stack() { return stack; }
    public RemoteInventoryView before() { return before; }
    public RemoteInventoryView after() { return after; }
    public RemoteInventoryView personalBefore() { return personalBefore; }
    public RemoteInventoryView personalAfter() { return personalAfter; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteDispenserLoad)) return false;
        RemoteDispenserLoad value = (RemoteDispenserLoad) other;
        return personalSlot == value.personalSlot && dispenserSlot == value.dispenserSlot
                && takeAction == value.takeAction && storeAction == value.storeAction
                && stack.equals(value.stack) && before.equals(value.before) && after.equals(value.after)
                && personalBefore.equals(value.personalBefore) && personalAfter.equals(value.personalAfter);
    }
    @Override public int hashCode() {
        return Objects.hash(personalSlot, dispenserSlot, takeAction, storeAction, stack,
                before, after, personalBefore, personalAfter); }
    private static boolean same(RemoteInventorySlot left, RemoteInventorySlot right) {
        return left.empty() ? right.empty() : !right.empty() && left.item().equals(right.item()); }
}
