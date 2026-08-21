package worldline.api;

/** Immutable authoritative state recovered after one rejected personal click. */
public final class RemoteRejectedTransaction {
    private final int actionId, slot;
    private final RemoteItemStack stalePrediction, cursorBefore, cursorAfter;
    private final RemoteInventoryView before, authoritative;

    public RemoteRejectedTransaction(int actionId, int slot, RemoteItemStack stalePrediction,
            RemoteInventoryView before, RemoteInventoryView authoritative,
            RemoteItemStack cursorBefore, RemoteItemStack cursorAfter) {
        if (actionId < 1 || actionId > 32767 || slot < 9 || slot > 44)
            throw new IllegalArgumentException("invalid rejected transaction identity");
        if (before == null || authoritative == null || before.windowId() != 0
                || authoritative.windowId() != 0 || before.size() != 45 || authoritative.size() != 45)
            throw new IllegalArgumentException("invalid rejected transaction windows");
        this.actionId = actionId; this.slot = slot; this.stalePrediction = stalePrediction;
        this.before = before; this.authoritative = authoritative;
        this.cursorBefore = cursorBefore; this.cursorAfter = cursorAfter;
    }

    public int actionId() { return actionId; }
    public int slot() { return slot; }
    public boolean stalePredictionEmpty() { return stalePrediction == null; }
    public RemoteItemStack stalePrediction() { return present(stalePrediction, "stale prediction is empty"); }
    public RemoteInventoryView before() { return before; }
    public RemoteInventoryView authoritative() { return authoritative; }
    public boolean cursorBeforeEmpty() { return cursorBefore == null; }
    public RemoteItemStack cursorBefore() { return present(cursorBefore, "cursor before is empty"); }
    public boolean cursorAfterEmpty() { return cursorAfter == null; }
    public RemoteItemStack cursorAfter() { return present(cursorAfter, "recovered cursor is empty"); }

    private static RemoteItemStack present(RemoteItemStack item, String message) {
        if (item == null) throw new IllegalStateException(message); return item; }
}
