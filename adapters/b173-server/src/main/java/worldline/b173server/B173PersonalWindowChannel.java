package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalCraft;
import worldline.api.RemotePersonalTransaction;

/** Exact left-click predictor for bounded personal-window take/place transitions. */
final class B173PersonalWindowChannel {
    private final DataOutputStream output;
    private final B173PlayInbound inbound;
    private int action;

    B173PersonalWindowChannel(DataOutputStream output, B173PlayInbound inbound) {
        this.output = output; this.inbound = inbound; }

    RemotePersonalTransaction click(int slot) throws IOException { return click(slot, false); }
    RemotePersonalTransaction rejectedTakeProbe(int slot) throws IOException { return click(slot, true); }

    RemotePersonalCraft craft2x2(int slot) throws IOException {
        RemoteInventoryView before = inbound.inventory(); RemoteItemStack log = new RemoteItemStack(17, 1, 0);
        if (action > 32763 || slot < 9 || slot > 44 || !inbound.cursorObserved() || inbound.cursor() != null
                || before.slot(slot).empty() || !before.slot(slot).item().equals(log) || !emptyCraft(before))
            throw new IllegalStateException("personal 2x2 craft requires one log and an empty matrix/cursor");
        RemotePersonalTransaction take = click(slot, false); RemoteItemStack planks = new RemoteItemStack(5, 4, 0);
        RemoteInventoryView matrix = view(take.after(), 0, planks, 1, log);
        B173PersonalStep place = step(1, null, matrix, null);
        RemoteInventoryView crafted = view(place.after, 0, null, 1, null);
        B173PersonalStep result = step(0, planks, crafted, planks);
        RemotePersonalTransaction stored = click(slot, false);
        return new RemotePersonalCraft(slot, take.actionId(), place.action, result.action, stored.actionId(),
                log, planks, before, matrix, crafted, stored.after());
    }

    private RemotePersonalTransaction click(int slot, boolean staleEmptyPrediction) throws IOException {
        RemoteInventoryView before = inbound.inventory();
        if (before.windowId() != 0 || before.size() != 45 || slot < 9 || slot > 44)
            throw new IllegalArgumentException("invalid personal inventory slot");
        if (!inbound.cursorObserved()) throw new IllegalStateException("personal cursor is not observed");
        RemoteItemStack cursor = inbound.cursor();
        RemoteItemStack source = before.slot(slot).empty() ? null : before.slot(slot).item();
        if ((source == null) == (cursor == null))
            throw new IllegalStateException("personal left click requires exactly one occupied side");
        RemoteItemStack predicted = source, nextCursor = source, nextSlot = cursor;
        if (staleEmptyPrediction && (source == null || cursor != null))
            throw new IllegalStateException("rejected-take probe requires occupied source and empty cursor");
        RemoteItemStack wirePrediction = staleEmptyPrediction ? null : predicted;
        List<RemoteInventorySlot> slots = new ArrayList<>(before.slots());
        slots.set(slot, new RemoteInventorySlot(slot, nextSlot));
        RemoteInventoryView after = new RemoteInventoryView(0, slots);
        if (action == 32767) throw new IllegalStateException("personal transaction counter exhausted");
        int nextAction = action + 1;
        inbound.beginPersonalTransaction(nextAction, slot, wirePrediction, before, after, cursor, nextCursor);
        output.writeByte(102); output.writeByte(0); output.writeShort(slot); output.writeByte(0);
        output.writeShort(nextAction); output.writeBoolean(false); B173InventoryCodec.item(output, wirePrediction);
        output.flush(); action = nextAction; return inbound.awaitPersonalTransaction();
    }

    private B173PersonalStep step(int slot, RemoteItemStack predicted,
            RemoteInventoryView after, RemoteItemStack cursorAfter) throws IOException {
        RemoteInventoryView before = inbound.inventory(); RemoteItemStack cursorBefore = inbound.cursor();
        if (action == 32767) throw new IllegalStateException("personal transaction counter exhausted");
        int nextAction = action + 1;
        inbound.beginPersonalTransaction(nextAction, slot, predicted, before, after, cursorBefore, cursorAfter);
        output.writeByte(102); output.writeByte(0); output.writeShort(slot); output.writeByte(0);
        output.writeShort(nextAction); output.writeBoolean(false); B173InventoryCodec.item(output, predicted);
        output.flush(); action = nextAction; return inbound.awaitPersonalStep();
    }

    private static RemoteInventoryView view(RemoteInventoryView source, int firstSlot,
            RemoteItemStack first, int secondSlot, RemoteItemStack second) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots());
        slots.set(firstSlot, new RemoteInventorySlot(firstSlot, first));
        slots.set(secondSlot, new RemoteInventorySlot(secondSlot, second));
        return new RemoteInventoryView(0, slots);
    }

    private static boolean emptyCraft(RemoteInventoryView view) {
        for (int slot = 0; slot < 5; slot++) if (!view.slot(slot).empty()) return false; return true; }
}
