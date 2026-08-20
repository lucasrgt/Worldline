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

/** Four accepted window-0 clicks that mill one bone 352 into bone meal 351x3:15. */
public final class B173BoneMealCraft {
    private static final RemoteItemStack BONE = new RemoteItemStack(352, 1, 0);
    private static final RemoteItemStack MEAL = new RemoteItemStack(351, 3, 15);
    private static final int[] CLICK_SLOTS = {44, 1, 0, 44};
    private static final int[] CLICK_ITEMS = {352, -1, 351, -1};
    private static final int[] CLICK_COUNTS = {1, 0, 3, 0};
    private static final int[] CLICK_DAMAGE = {0, 0, 15, 0};

    private B173BoneMealCraft() {}

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
        if (input.available() != 0 || bytes.size() != 46)
            throw new AssertionError("bone-meal Packet102 sequence size drifted");
    }

    public static void apply(B173WireClient actor) {
        try {
            B173PlayChannel channel = actor.channel();
            B173PlayInbound inbound = channel.inbound();
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("bone-meal craft preflight failed");
            int slot = boneSlot(inbound.inventory());
            if (slot < 9) throw new IllegalStateException("bone 352 absent from personal storage");
            mill(channel, inbound, slot, 1);
            if (inbound.cursor() != null || !same(inbound.inventory(), slot, MEAL) || !emptyCraft(inbound.inventory()))
                throw new IllegalStateException("bone-meal craft result drifted");
        } catch (IOException error) {
            throw new IllegalStateException("bone-meal craft failed", error);
        }
    }

    private static void mill(B173PlayChannel channel, B173PlayInbound inbound, int slot, int action)
            throws IOException {
        RemoteInventoryView before = inbound.inventory();
        if (inbound.cursor() != null || !same(before, slot, BONE) || !emptyCraft(before))
            throw new IllegalStateException("bone-meal ingredients drifted");
        step(channel, inbound, slot, BONE, replace(before, slot, null), BONE, action++);
        RemoteInventoryView matrix = replace(replace(inbound.inventory(), 1, BONE), 0, MEAL);
        step(channel, inbound, 1, null, matrix, null, action++);
        RemoteInventoryView crafted = replace(replace(inbound.inventory(), 0, null), 1, null);
        step(channel, inbound, 0, MEAL, crafted, MEAL, action++);
        step(channel, inbound, slot, null, replace(inbound.inventory(), slot, MEAL), null, action);
        if (action != 4) throw new IllegalStateException("bone-meal action drift");
    }

    private static void step(B173PlayChannel channel, B173PlayInbound inbound, int slot,
            RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter, int action)
            throws IOException {
        inbound.beginPersonalTransaction(action, slot, predicted, inbound.inventory(), after,
                inbound.cursor(), cursorAfter);
        B173ContainerPacket.write(channel.output, 0, slot, 0, action, predicted);
        channel.output.flush();
        inbound.awaitPersonalStep();
    }

    private static void packet(DataInputStream input, int slot, int action, int item, int count, int damage)
            throws Exception {
        if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 0 || input.readShort() != slot
                || input.readUnsignedByte() != 0 || input.readShort() != action || input.readBoolean()
                || input.readShort() != item)
            throw new AssertionError("bone-meal Packet102 fields drifted");
        if (item >= 0 && (input.readUnsignedByte() != count || input.readShort() != damage))
            throw new AssertionError("bone-meal Packet102 stack drifted");
    }

    private static int boneSlot(RemoteInventoryView view) {
        for (int slot = 9; slot <= 44; slot++) if (same(view, slot, BONE)) return slot;
        return -1;
    }

    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
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
