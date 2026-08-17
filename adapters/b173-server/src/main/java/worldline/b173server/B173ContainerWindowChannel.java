package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteChestTransfer;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Exact two-click accepted transfer from personal storage into a single chest. */
final class B173ContainerWindowChannel {
    private final DataOutputStream output; private final B173PlayInbound inbound;
    private int windowId = -1, action; private long epoch = -1L;
    B173ContainerWindowChannel(DataOutputStream output, B173PlayInbound inbound) {
        this.output = output; this.inbound = inbound; }

    RemoteChestTransfer store(int personalSlot, int chestSlot) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        if (active.descriptor().kind() != RemoteWindowKind.CHEST || before.size() != 63
                || personalSlot < 9 || personalSlot > 44 || chestSlot < 0 || chestSlot > 26
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid chest transfer boundary");
        long activeEpoch = inbound.activeWindowEpoch();
        if (epoch != activeEpoch) { epoch = activeEpoch; windowId = before.windowId(); action = 0; }
        if (action > 32765) throw new IllegalStateException("container transaction counter exhausted");
        int combined = personalSlot + 18;
        if (before.slot(combined).empty() || !before.slot(chestSlot).empty())
            throw new IllegalStateException("chest transfer requires occupied source and empty target");
        RemoteItemStack stack = before.slot(combined).item(); RemoteInventoryView personal = inbound.inventory();
        RemoteInventoryView taken = replace(before, combined, null), personalTaken = replace(personal, personalSlot, null);
        B173ContainerStep take = step(combined, stack, taken, personal, personalTaken, stack);
        RemoteInventoryView stored = replace(take.after, chestSlot, stack);
        B173ContainerStep put = step(chestSlot, null, stored, personalTaken, personalTaken, null);
        return new RemoteChestTransfer(personalSlot, chestSlot, take.action, put.action,
                stack, before, put.after);
    }

    private B173ContainerStep step(int slot, RemoteItemStack predicted, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter,
            RemoteItemStack cursorAfter) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        RemoteItemStack cursorBefore = inbound.cursor(); int nextAction = action + 1;
        B173ContainerStep step = new B173ContainerStep(windowId, nextAction, slot, predicted,
                before, after, personalBefore, personalAfter, cursorBefore, cursorAfter);
        inbound.beginContainerTransaction(step); output.writeByte(102); output.writeByte(windowId);
        output.writeShort(slot); output.writeByte(0); output.writeShort(nextAction); output.writeBoolean(false);
        B173InventoryCodec.item(output, predicted); output.flush(); action = nextAction;
        return inbound.awaitContainerTransaction();
    }
    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item)); return new RemoteInventoryView(source.windowId(), slots); }
}
