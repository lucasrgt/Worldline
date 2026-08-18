package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteChestTransfer;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteFurnaceLoad;
import worldline.api.RemoteFurnaceExtraction;
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
        prepare(before, 2); Move move = move(personalSlot, chestSlot);
        return new RemoteChestTransfer(personalSlot, chestSlot, move.take.action, move.put.action,
                move.stack, before, move.put.after);
    }

    RemoteFurnaceLoad loadFurnace(int inputPersonalSlot, int fuelPersonalSlot) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        RemoteInventoryView personalBefore = inbound.inventory();
        RemoteItemStack sand = new RemoteItemStack(12, 1, 0), coal = new RemoteItemStack(263, 1, 0);
        if (active.descriptor().kind() != RemoteWindowKind.FURNACE || before.size() != 39
                || inputPersonalSlot < 9 || inputPersonalSlot > 44 || fuelPersonalSlot < 9
                || fuelPersonalSlot > 44 || inputPersonalSlot == fuelPersonalSlot
                || before.slot(inputPersonalSlot - 6).empty() || before.slot(fuelPersonalSlot - 6).empty()
                || !before.slot(inputPersonalSlot - 6).item().equals(sand)
                || !before.slot(fuelPersonalSlot - 6).item().equals(coal)
                || !before.slot(0).empty() || !before.slot(1).empty() || !before.slot(2).empty()
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid furnace load boundary");
        prepare(before, 4); Move input = move(inputPersonalSlot, 0); Move fuel = move(fuelPersonalSlot, 1);
        return new RemoteFurnaceLoad(inputPersonalSlot, fuelPersonalSlot, input.take.action,
                input.put.action, fuel.take.action, fuel.put.action, input.stack, fuel.stack,
                before, fuel.put.after, personalBefore, inbound.inventory());
    }

    RemoteFurnaceExtraction takeFurnaceOutput(int personalSlot) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        RemoteInventoryView personalBefore = inbound.inventory(); int combined = personalSlot - 6;
        RemoteItemStack glass = new RemoteItemStack(20, 1, 0);
        if (active.descriptor().kind() != RemoteWindowKind.FURNACE || before.size() != 39
                || personalSlot < 9 || personalSlot > 44 || !before.slot(0).empty()
                || !before.slot(1).empty() || before.slot(2).empty()
                || !before.slot(2).item().equals(glass) || !before.slot(combined).empty()
                || !personalBefore.slot(personalSlot).empty()
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid furnace extraction boundary");
        prepare(before, 2); RemoteInventoryView taken = replace(before, 2, null);
        B173ContainerStep take = step(2, glass, taken, personalBefore, personalBefore, glass,
                16842772, 1); RemoteInventoryView stored = replace(take.after, combined, glass);
        RemoteInventoryView personalStored = replace(personalBefore, personalSlot, glass);
        B173ContainerStep put = step(combined, null, stored, personalBefore, personalStored, null);
        return new RemoteFurnaceExtraction(personalSlot, take.action, put.action, 1, glass,
                before, put.after, personalBefore, personalStored);
    }

    private void prepare(RemoteInventoryView view, int actions) {
        long activeEpoch = inbound.activeWindowEpoch();
        if (epoch != activeEpoch) { epoch = activeEpoch; windowId = view.windowId(); action = 0; }
        if (action > 32767 - actions) throw new IllegalStateException("container transaction counter exhausted");
    }

    private Move move(int personalSlot, int ownedSlot) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        int owned = active.descriptor().playerTailOffset(), combined = owned + personalSlot - 9;
        if (personalSlot < 9 || personalSlot > 44 || ownedSlot < 0 || ownedSlot >= owned
                || before.slot(combined).empty() || !before.slot(ownedSlot).empty())
            throw new IllegalStateException("container move requires occupied source and empty target");
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
        inbound.beginContainerTransaction(step); output.writeByte(102); output.writeByte(windowId);
        output.writeShort(slot); output.writeByte(0); output.writeShort(nextAction); output.writeBoolean(false);
        B173InventoryCodec.item(output, predicted); output.flush(); action = nextAction;
        return inbound.awaitContainerTransaction();
    }
    private B173ContainerStep step(int slot, RemoteItemStack predicted, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter,
            RemoteItemStack cursorAfter, int statisticId, int statisticIncrement) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        RemoteItemStack cursorBefore = inbound.cursor(); int nextAction = action + 1;
        B173ContainerStep step = new B173ContainerStep(windowId, nextAction, slot, predicted,
                before, after, personalBefore, personalAfter, cursorBefore, cursorAfter,
                statisticId, statisticIncrement); send(step); action = nextAction;
        return inbound.awaitContainerTransaction();
    }
    private void send(B173ContainerStep step) throws IOException {
        inbound.beginContainerTransaction(step); output.writeByte(102); output.writeByte(windowId);
        output.writeShort(step.slot); output.writeByte(0); output.writeShort(step.action);
        output.writeBoolean(false); B173InventoryCodec.item(output, step.predicted); output.flush();
    }
    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item)); return new RemoteInventoryView(source.windowId(), slots); }
    private static final class Move { final RemoteItemStack stack; final B173ContainerStep take, put;
        Move(RemoteItemStack stack, B173ContainerStep take, B173ContainerStep put) {
            this.stack = stack; this.take = take; this.put = put; } }
}
