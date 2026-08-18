package worldline.api;

import java.util.Objects;

/** Immutable accepted slabs output take, ingredient consumption, and storage. */
public final class RemoteWorkbenchOutput {
    private final int personalSlot, takeAction, storeAction, craftedCount;
    private final RemoteItemStack stack;
    private final RemoteInventoryView before, consumed, after, personalBefore, personalAfter;
    public RemoteWorkbenchOutput(int personalSlot, int takeAction, int storeAction,
            int craftedCount, RemoteItemStack stack, RemoteInventoryView before,
            RemoteInventoryView consumed, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter) {
        RemoteItemStack slabs = new RemoteItemStack(44, 3, 2), plank = new RemoteItemStack(5, 1, 0);
        if (personalSlot < 9 || personalSlot > 44 || takeAction < 1 || takeAction > 32766
                || storeAction != takeAction + 1 || craftedCount != 3 || !slabs.equals(stack)
                || !container(before) || !container(consumed) || !container(after)
                || before.windowId() != consumed.windowId() || before.windowId() != after.windowId()
                || !personal(personalBefore) || !personal(personalAfter))
            throw new IllegalArgumentException("invalid workbench output identity");
        int combined = personalSlot + 1;
        if (!item(before, 0, slabs) || !item(before, 1, plank) || !item(before, 2, plank)
                || !item(before, 3, plank) || !before.slot(combined).empty()
                || !personalBefore.slot(personalSlot).empty() || !emptyOwned(consumed) || !consumed.slot(combined).empty()
                || !emptyOwned(after)
                || !item(after, combined, slabs) || !item(personalAfter, personalSlot, slabs))
            throw new IllegalArgumentException("invalid workbench output state");
        for (int slot = 4; slot < 10; slot++) if (!before.slot(slot).empty())
            throw new IllegalArgumentException("unrelated workbench matrix slot occupied");
        for (int slot = 0; slot < 46; slot++) if (slot > 3 && slot != combined
                && !before.slot(slot).equals(after.slot(slot)))
            throw new IllegalArgumentException("workbench output changed unrelated combined slot");
        for (int slot = 0; slot < 45; slot++) if (slot != personalSlot
                && !personalBefore.slot(slot).equals(personalAfter.slot(slot)))
            throw new IllegalArgumentException("workbench output changed unrelated personal slot");
        if (!tail(before, personalBefore) || !tail(consumed, personalBefore) || !tail(after, personalAfter))
            throw new IllegalArgumentException("workbench output tail drift");
        this.personalSlot = personalSlot; this.takeAction = takeAction; this.storeAction = storeAction;
        this.craftedCount = craftedCount; this.stack = stack; this.before = before; this.consumed = consumed; this.after = after;
        this.personalBefore = personalBefore; this.personalAfter = personalAfter;
    }
    public int personalSlot() { return personalSlot; } public int takeAction() { return takeAction; }
    public int storeAction() { return storeAction; } public int craftedCount() { return craftedCount; }
    public RemoteItemStack stack() { return stack; } public RemoteInventoryView before() { return before; }
    public RemoteInventoryView consumed() { return consumed; }
    public RemoteInventoryView after() { return after; } public RemoteInventoryView personalBefore() { return personalBefore; }
    public RemoteInventoryView personalAfter() { return personalAfter; }
    private static boolean container(RemoteInventoryView view) { return view != null
            && view.windowId() >= 1 && view.windowId() <= 100 && view.size() == 46; }
    private static boolean personal(RemoteInventoryView view) { return view != null
            && view.windowId() == 0 && view.size() == 45; }
    private static boolean item(RemoteInventoryView view, int slot, RemoteItemStack expected) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(expected); }
    private static boolean emptyOwned(RemoteInventoryView view) { for (int slot = 0; slot < 10; slot++)
        if (!view.slot(slot).empty()) return false; return true; }
    private static boolean tail(RemoteInventoryView combined, RemoteInventoryView personal) {
        for (int slot = 9; slot <= 44; slot++) { RemoteInventorySlot left = combined.slot(slot + 1), right = personal.slot(slot);
            if (left.empty() != right.empty() || !left.empty() && !left.item().equals(right.item())) return false; }
        return true; }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteWorkbenchOutput)) return false;
        RemoteWorkbenchOutput value = (RemoteWorkbenchOutput) other; return personalSlot == value.personalSlot
                && takeAction == value.takeAction && storeAction == value.storeAction
                && craftedCount == value.craftedCount && stack.equals(value.stack) && before.equals(value.before)
                && consumed.equals(value.consumed) && after.equals(value.after) && personalBefore.equals(value.personalBefore)
                && personalAfter.equals(value.personalAfter); }
    @Override public int hashCode() { return Objects.hash(personalSlot, takeAction, storeAction,
            craftedCount, stack, before, consumed, after, personalBefore, personalAfter); }
}
