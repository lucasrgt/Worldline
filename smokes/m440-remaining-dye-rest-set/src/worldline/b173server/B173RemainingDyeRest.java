package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Twenty accepted window-0 clicks that mix remaining dyes in the personal 2x2 grid. */
public final class B173RemainingDyeRest {
    private static final int[] CLICK_SLOTS = {36, 1, 37, 2, 38, 3, 0, 36, 39, 1, 40, 2, 0, 39, 41, 1, 42, 2, 0, 41};
    private static final int[] CLICK_ITEMS = {351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1};
    private static final int[] CLICK_COUNTS = {1, 0, 1, 0, 1, 0, 3, 0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0, 2, 0};
    private static final int[] CLICK_DAMAGE = {0, 0, 15, 0, 15, 0, 7, 0, 8, 0, 15, 0, 7, 0, 5, 0, 9, 0, 13, 0};

    private B173RemainingDyeRest() {}

    public static void verify() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        for (int index = 0; index < CLICK_SLOTS.length; index++) {
            int item = CLICK_ITEMS[index];
            B173ContainerPacket.write(output, 0, CLICK_SLOTS[index], 0, index + 1,
                    item < 0 ? null : new RemoteItemStack(item, CLICK_COUNTS[index], CLICK_DAMAGE[index]));
        }
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        for (int index = 0; index < CLICK_SLOTS.length; index++)
            packet(input, CLICK_SLOTS[index], index + 1, CLICK_ITEMS[index], CLICK_COUNTS[index], CLICK_DAMAGE[index]);
        if (input.available() != 0 || bytes.size() != 230)
            throw new AssertionError("remaining-dye-rest Packet102 sequence size drifted");
    }

    public static void apply(B173WireClient actor) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("remaining-dye-rest craft preflight failed");
            int action = mix(channel, inbound, new int[] {36, 37, 38}, new int[] {0, 15, 15}, 3, 7, 1);
            action = mix(channel, inbound, new int[] {39, 40}, new int[] {8, 15}, 2, 7, action);
            action = mix(channel, inbound, new int[] {41, 42}, new int[] {5, 9}, 2, 13, action);
            if (action != 21 || inbound.cursor() != null)
                throw new IllegalStateException("remaining-dye-rest action drift");
            RemoteInventoryView view = inbound.inventory();
            if (view.occupiedSlots() != 3 || !same(view, 36, 351, 3, 7) || !same(view, 39, 351, 2, 7)
                    || !same(view, 41, 351, 2, 13) || !emptyCraft(view) || !view.slot(37).empty()
                    || !view.slot(38).empty() || !view.slot(40).empty() || !view.slot(42).empty())
                throw new IllegalStateException("remaining-dye-rest results drifted");
        } catch (IOException error) {
            throw new IllegalStateException("remaining-dye-rest craft failed", error);
        }
    }

    private static int mix(B173PlayChannel channel, B173PlayInbound inbound, int[] slots, int[] damages,
            int resultCount, int resultDamage, int action) throws IOException {
        RemoteItemStack result = new RemoteItemStack(351, resultCount, resultDamage);
        RemoteInventoryView before = inbound.inventory();
        if (inbound.cursor() != null || !emptyCraft(before) || result.legacyId() != 351)
            throw new IllegalStateException("remaining-dye-rest ingredients drifted");
        for (int index = 0; index < slots.length; index++)
            if (slots[index] < 9 || slots[index] > 44 || !same(before, slots[index], 351, 1, damages[index]))
                throw new IllegalStateException("remaining-dye-rest ingredients drifted");
        for (int index = 0; index < slots.length; index++) {
            RemoteItemStack item = new RemoteItemStack(351, 1, damages[index]);
            step(channel, inbound, slots[index], item, replace(inbound.inventory(), slots[index], null), item, action++);
            RemoteInventoryView placed = replace(inbound.inventory(), index + 1, item);
            if (index == slots.length - 1) placed = replace(placed, 0, result);
            step(channel, inbound, index + 1, null, placed, null, action++);
        }
        RemoteInventoryView crafted = inbound.inventory();
        for (int slot = 0; slot <= slots.length; slot++) crafted = replace(crafted, slot, null);
        step(channel, inbound, 0, result, crafted, result, action++);
        step(channel, inbound, slots[0], null, replace(inbound.inventory(), slots[0], result), null, action++);
        if (!same(inbound.inventory(), slots[0], result) || !emptyCraft(inbound.inventory())
                || inbound.cursor() != null)
            throw new IllegalStateException("remaining-dye-rest store drifted");
        for (int index = 1; index < slots.length; index++)
            if (!inbound.inventory().slot(slots[index]).empty())
                throw new IllegalStateException("remaining-dye-rest store drifted");
        return action;
    }

    private static void step(B173PlayChannel channel, B173PlayInbound inbound, int slot,
            RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter, int action)
            throws IOException {
        inbound.beginPersonalTransaction(action, slot, predicted, inbound.inventory(), after,
                inbound.cursor(), cursorAfter);
        B173ContainerPacket.write(channel.output, 0, slot, 0, action, predicted);
        channel.output.flush(); inbound.awaitPersonalStep();
    }

    private static void packet(DataInputStream input, int slot, int action, int item, int count, int damage)
            throws Exception {
        if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 0 || input.readShort() != slot
                || input.readUnsignedByte() != 0 || input.readShort() != action || input.readBoolean()
                || input.readShort() != item)
            throw new AssertionError("remaining-dye-rest Packet102 fields drifted");
        if (item >= 0 && (input.readUnsignedByte() != count || input.readShort() != damage))
            throw new AssertionError("remaining-dye-rest Packet102 stack drifted");
    }

    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
    }

    private static boolean same(RemoteInventoryView view, int slot, int id, int count, int damage) {
        return same(view, slot, new RemoteItemStack(id, count, damage));
    }

    private static boolean emptyCraft(RemoteInventoryView view) {
        for (int slot = 0; slot < 5; slot++) if (!view.slot(slot).empty()) return false;
        return true;
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(0, slots);
    }
}
