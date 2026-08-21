package worldline.b173server;

import java.io.IOException;

/** Correlates one accepted active-container Packet102/106 prediction. */
final class B173ContainerTransactionTracker {
    private B173ContainerStep pending, accepted;
    private boolean statisticObserved;
    void begin(B173ContainerStep step) {
        if (pending != null || accepted != null) throw new IllegalStateException("container transaction pending");
        pending = step; statisticObserved = false;
    }
    boolean pending() { return pending != null; }
    void statistic(int id, int increment) throws IOException {
        if (pending == null || pending.statisticId < 0 || id != pending.statisticId) return;
        if (increment != pending.statisticIncrement || statisticObserved)
            throw new IOException("container transaction statistic drift");
        statisticObserved = true;
    }
    void acknowledge(int windowId, int action, boolean allowed, B173InventoryTracker inventory,
            B173WindowTracker windows) throws IOException {
        if (pending == null || windowId != pending.windowId || action != pending.action)
            throw new IOException("container transaction acknowledgement drift");
        if (!allowed) throw new IOException("container transaction rejected");
        if (pending.statisticId >= 0 && !statisticObserved)
            throw new IOException("container transaction statistic absent");
        if (!windows.matches(pending.before)
                || !inventory.matches(pending.personalBefore, pending.cursorBefore))
            throw new IOException("container transaction base state drift");
        windows.adopt(pending.after); inventory.adopt(pending.personalAfter, pending.cursorAfter);
        accepted = pending; pending = null; statisticObserved = false;
    }
    B173ContainerStep take() { B173ContainerStep result = accepted; accepted = null; return result; }
}
