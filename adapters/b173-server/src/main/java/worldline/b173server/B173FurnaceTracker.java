package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Coalesces bounded furnace progress and completes on an exact output slot. */
final class B173FurnaceTracker {
    private long epoch = -1L;
    private int windowId, maximumCook, maximumBurn, totalBurn, completionBurn;
    private boolean cookStarted, completionReset;

    void progress(DataInputStream input, B173WindowTracker windows) throws IOException {
        int packetWindow = input.readByte(), property = input.readShort(), value = input.readShort();
        RemoteContainerWindow active = windows.activeWindow(); long activeEpoch = windows.activeEpoch();
        if (active.descriptor().kind() != RemoteWindowKind.FURNACE
                || packetWindow != active.descriptor().windowId() || property < 0 || property > 2
                || value < 0 || property == 0 && value > 199
                || property == 1 && value > 1600
                || property == 2 && value != 0 && value != 1600)
            throw new IOException("furnace progress drift");
        if (epoch != activeEpoch) { epoch = activeEpoch; windowId = packetWindow;
            maximumCook = 0; maximumBurn = 0; totalBurn = 0; completionBurn = 0;
            cookStarted = false; completionReset = false; }
        if (property == 0) { if (value > 0) cookStarted = true;
            else if (cookStarted && maximumCook == 199) completionReset = true;
            maximumCook = Math.max(maximumCook, value); }
        else if (property == 1) { maximumBurn = Math.max(maximumBurn, value);
            if (completionReset && completionBurn == 0) completionBurn = value; }
        else totalBurn = Math.max(totalBurn, value);
    }

    RemoteFurnaceSmelt ready(B173WindowTracker windows) {
        RemoteContainerWindow active = windows.activeWindow();
        RemoteItemStack expected = new RemoteItemStack(20, 1, 0);
        if (active.descriptor().kind() != RemoteWindowKind.FURNACE
                || epoch != windows.activeEpoch() || active.descriptor().windowId() != windowId
                || active.inventory().slot(2).empty()
                || !active.inventory().slot(2).item().equals(expected)
                || !active.inventory().slot(0).empty() || !active.inventory().slot(1).empty()
                || !completionReset || maximumCook != 199 || maximumBurn != 1600
                || totalBurn != 1600 || completionBurn != 1401) return null;
        return new RemoteFurnaceSmelt(active, expected, maximumCook, maximumBurn, totalBurn, completionBurn);
    }
}
