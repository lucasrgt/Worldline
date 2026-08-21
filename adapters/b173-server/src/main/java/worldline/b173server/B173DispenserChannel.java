package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteDispenserLoad;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Exact two-click accepted transfer from personal storage into one dispenser slot. */
final class B173DispenserChannel {
    private final DataOutputStream output;
    private final B173PlayInbound inbound;
    private int windowId = -1, action;
    private long epoch = -1L;

    B173DispenserChannel(DataOutputStream output, B173PlayInbound inbound) {
        this.output = output; this.inbound = inbound; }

    RemoteDispenserLoad load(int personalSlot, int dispenserSlot) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow();
        RemoteInventoryView before = active.inventory();
        RemoteInventoryView personalBefore = inbound.inventory();
        if (active.descriptor().kind() != RemoteWindowKind.DISPENSER || before.size() != 45
                || personalSlot < 9 || personalSlot > 44 || dispenserSlot < 0 || dispenserSlot > 8
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid dispenser load boundary");
        prepare(before, 2); Move move = move(personalSlot, dispenserSlot);
        return new RemoteDispenserLoad(personalSlot, dispenserSlot, move.take.action, move.put.action,
                move.stack, before, move.put.after, personalBefore, inbound.inventory());
    }

    private void prepare(RemoteInventoryView view, int actions) {
        long activeEpoch = inbound.activeWindowEpoch();
        if (epoch != activeEpoch) { epoch = activeEpoch; windowId = view.windowId(); action = 0; }
        if (action > 32767 - actions) throw new IllegalStateException("dispenser transaction counter exhausted");
    }

    private Move move(int personalSlot, int ownedSlot) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        int owned = active.descriptor().playerTailOffset(), combined = owned + personalSlot - 9;
        if (personalSlot < 9 || personalSlot > 44 || ownedSlot < 0 || ownedSlot >= owned
                || before.slot(combined).empty() || !before.slot(ownedSlot).empty())
            throw new IllegalStateException("dispenser move requires occupied source and empty target");
        RemoteItemStack stack = before.slot(combined).item(); RemoteInventoryView personal = inbound.inventory();
        RemoteInventoryView taken = replace(before, combined, null), personalTaken = replace(personal, personalSlot, null);
        B173ContainerStep take = step(combined, stack, taken, personal, personalTaken, stack);
        B173ContainerStep put = step(ownedSlot, null, replace(take.after, ownedSlot, stack),
                personalTaken, personalTaken, null);
        return new Move(stack, take, put);
    }

    private B173ContainerStep step(int slot, RemoteItemStack predicted, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter,
            RemoteItemStack cursorAfter) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        RemoteItemStack cursorBefore = inbound.cursor(); int nextAction = action + 1;
        B173ContainerStep step = new B173ContainerStep(windowId, nextAction, slot, predicted,
                before, after, personalBefore, personalAfter, cursorBefore, cursorAfter);
        inbound.beginContainerTransaction(step); B173ContainerPacket.write(output, windowId, slot, 0,
                nextAction, predicted); output.flush(); action = nextAction;
        return inbound.awaitContainerTransaction();
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(source.windowId(), slots); }

    private static final class Move {
        final RemoteItemStack stack; final B173ContainerStep take, put;
        Move(RemoteItemStack stack, B173ContainerStep take, B173ContainerStep put) {
            this.stack = stack; this.take = take; this.put = put; }
    }
}
