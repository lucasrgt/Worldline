package worldline.api;

import java.util.Objects;

/** Immutable accepted left-click transition in the personal inventory window. */
public final class RemotePersonalTransaction {
    private final int actionId, slot;
    private final RemoteItemStack predicted, cursorBefore, cursorAfter;
    private final RemoteInventoryView before, after;

    public RemotePersonalTransaction(int actionId, int slot, RemoteItemStack predicted,
            RemoteInventoryView before, RemoteInventoryView after,
            RemoteItemStack cursorBefore, RemoteItemStack cursorAfter) {
        if (actionId < 1 || actionId > 32767 || slot < 9 || slot > 44)
            throw new IllegalArgumentException("invalid personal transaction identity");
        if (before == null || after == null || before.windowId() != 0 || after.windowId() != 0
                || before.size() != 45 || after.size() != 45)
            throw new IllegalArgumentException("invalid personal transaction windows");
        for (int index = 0; index < 45; index++) if (index != slot
                && !before.slot(index).equals(after.slot(index)))
            throw new IllegalArgumentException("personal transaction changed unrelated slot");
        RemoteItemStack source = before.slot(slot).empty() ? null : before.slot(slot).item();
        RemoteItemStack target = after.slot(slot).empty() ? null : after.slot(slot).item();
        boolean take = cursorBefore == null && source != null && source.equals(predicted)
                && target == null && source.equals(cursorAfter);
        boolean place = cursorBefore != null && source == null && predicted == null
                && cursorBefore.equals(target) && cursorAfter == null;
        if (!take && !place) throw new IllegalArgumentException("invalid personal left-click transition");
        this.actionId = actionId; this.slot = slot; this.predicted = predicted;
        this.before = before; this.after = after;
        this.cursorBefore = cursorBefore; this.cursorAfter = cursorAfter;
    }

    public int actionId() { return actionId; }
    public int slot() { return slot; }
    public boolean predictedEmpty() { return predicted == null; }
    public RemoteItemStack predicted() { return present(predicted, "predicted result is empty"); }
    public RemoteInventoryView before() { return before; }
    public RemoteInventoryView after() { return after; }
    public boolean cursorBeforeEmpty() { return cursorBefore == null; }
    public RemoteItemStack cursorBefore() { return present(cursorBefore, "cursor before is empty"); }
    public boolean cursorAfterEmpty() { return cursorAfter == null; }
    public RemoteItemStack cursorAfter() { return present(cursorAfter, "cursor after is empty"); }

    private static RemoteItemStack present(RemoteItemStack item, String message) {
        if (item == null) throw new IllegalStateException(message); return item; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof RemotePersonalTransaction)) return false;
        RemotePersonalTransaction value = (RemotePersonalTransaction) other;
        return actionId == value.actionId && slot == value.slot
                && Objects.equals(predicted, value.predicted) && before.equals(value.before)
                && after.equals(value.after) && Objects.equals(cursorBefore, value.cursorBefore)
                && Objects.equals(cursorAfter, value.cursorAfter);
    }
    @Override public int hashCode() {
        return Objects.hash(actionId, slot, predicted, before, after, cursorBefore, cursorAfter); }
}
