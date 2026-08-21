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

/** Eighteen accepted window-0 clicks that mix remaining dyes in the personal 2x2 grid. */
public final class B173RemainingDyeMix {
    private static final int[] LEFT = {36, 38, 40}, RIGHT = {37, 39, 41};
    private static final int[] LEFT_DMG = {2, 1, 4}, RIGHT_DMG = {4, 15, 15}, RESULT_DMG = {6, 9, 12};
    private static final int[] CLICK_SLOTS = {36, 1, 37, 2, 0, 36, 38, 1, 39, 2, 0, 38, 40, 1, 41, 2, 0, 40};
    private static final int[] CLICK_ITEMS = {351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1, 351, -1};
    private static final int[] CLICK_COUNTS = {1, 0, 1, 0, 2, 0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0, 2, 0};
    private static final int[] CLICK_DAMAGE = {2, 0, 4, 0, 6, 0, 1, 0, 15, 0, 9, 0, 4, 0, 15, 0, 12, 0};

    private B173RemainingDyeMix() {}

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
        if (input.available() != 0 || bytes.size() != 207)
            throw new AssertionError("remaining-dye-mix Packet102 sequence size drifted");
    }

    public static void apply(B173WireClient actor) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("remaining-dye-mix craft preflight failed");
            int action = 1;
            for (int index = 0; index < RESULT_DMG.length; index++)
                action = mix(channel, inbound, LEFT[index], RIGHT[index], LEFT_DMG[index], RIGHT_DMG[index],
                        RESULT_DMG[index], action);
            if (action != 19 || inbound.cursor() != null)
                throw new IllegalStateException("remaining-dye-mix action drift");
            RemoteInventoryView view = inbound.inventory();
            if (view.occupiedSlots() != 3 || !same(view, 36, 351, 2, 6) || !same(view, 38, 351, 2, 9)
                    || !same(view, 40, 351, 2, 12) || !emptyCraft(view) || !view.slot(37).empty()
                    || !view.slot(39).empty() || !view.slot(41).empty())
                throw new IllegalStateException("remaining-dye-mix results drifted");
        } catch (IOException error) {
            throw new IllegalStateException("remaining-dye-mix craft failed", error);
        }
    }

    private static int mix(B173PlayChannel channel, B173PlayInbound inbound, int leftSlot, int rightSlot,
            int leftDamage, int rightDamage, int resultDamage, int action) throws IOException {
        RemoteItemStack left = new RemoteItemStack(351, 1, leftDamage);
        RemoteItemStack right = new RemoteItemStack(351, 1, rightDamage);
        RemoteItemStack result = new RemoteItemStack(351, 2, resultDamage);
        RemoteInventoryView before = inbound.inventory();
        if (leftSlot < 9 || rightSlot < 9 || leftSlot > 44 || rightSlot > 44 || inbound.cursor() != null
                || !same(before, leftSlot, left) || !same(before, rightSlot, right) || !emptyCraft(before)
                || result.legacyId() != 351 || result.count() != 2)
            throw new IllegalStateException("remaining-dye-mix ingredients drifted");
        step(channel, inbound, leftSlot, left, replace(before, leftSlot, null), left, action++);
        step(channel, inbound, 1, null, replace(inbound.inventory(), 1, left), null, action++);
        step(channel, inbound, rightSlot, right, replace(inbound.inventory(), rightSlot, null), right, action++);
        RemoteInventoryView matrix = replace(replace(inbound.inventory(), 2, right), 0, result);
        step(channel, inbound, 2, null, matrix, null, action++);
        RemoteInventoryView crafted = replace(replace(replace(inbound.inventory(), 0, null), 1, null), 2, null);
        step(channel, inbound, 0, result, crafted, result, action++);
        step(channel, inbound, leftSlot, null, replace(inbound.inventory(), leftSlot, result), null, action++);
        if (!same(inbound.inventory(), leftSlot, result) || !emptyCraft(inbound.inventory())
                || inbound.cursor() != null || !inbound.inventory().slot(rightSlot).empty())
            throw new IllegalStateException("remaining-dye-mix store drifted");
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
            throw new AssertionError("remaining-dye-mix Packet102 fields drifted");
        if (item >= 0 && (input.readUnsignedByte() != count || input.readShort() != damage))
            throw new AssertionError("remaining-dye-mix Packet102 stack drifted");
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
