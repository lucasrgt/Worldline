package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalTransaction;

/** Exact left-click predictor for bounded personal-window take/place transitions. */
final class B173PersonalWindowChannel {
    private final DataOutputStream output;
    private final B173PlayInbound inbound;
    private int action;

    B173PersonalWindowChannel(DataOutputStream output, B173PlayInbound inbound) {
        this.output = output; this.inbound = inbound; }

    RemotePersonalTransaction click(int slot) throws IOException {
        RemoteInventoryView before = inbound.inventory();
        if (before.windowId() != 0 || before.size() != 45 || slot < 9 || slot > 44)
            throw new IllegalArgumentException("invalid personal inventory slot");
        if (!inbound.cursorObserved()) throw new IllegalStateException("personal cursor is not observed");
        RemoteItemStack cursor = inbound.cursor();
        RemoteItemStack source = before.slot(slot).empty() ? null : before.slot(slot).item();
        if ((source == null) == (cursor == null))
            throw new IllegalStateException("personal left click requires exactly one occupied side");
        RemoteItemStack predicted = source, nextCursor = source, nextSlot = cursor;
        List<RemoteInventorySlot> slots = new ArrayList<>(before.slots());
        slots.set(slot, new RemoteInventorySlot(slot, nextSlot));
        RemoteInventoryView after = new RemoteInventoryView(0, slots);
        if (action == 32767) throw new IllegalStateException("personal transaction counter exhausted");
        int nextAction = action + 1;
        inbound.beginPersonalTransaction(nextAction, slot, predicted, before, after, cursor, nextCursor);
        output.writeByte(102); output.writeByte(0); output.writeShort(slot); output.writeByte(0);
        output.writeShort(nextAction); output.writeBoolean(false); B173InventoryCodec.item(output, predicted);
        output.flush(); action = nextAction; return inbound.awaitPersonalTransaction();
    }
}
