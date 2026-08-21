package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Accepted workbench take/place sequences that craft the wooden tool family. */
public final class B173WoodToolClick {
    public static final int SWORD = 268, SHOVEL = 269, PICK = 270, AXE = 271, HOE = 290;
    private static final int PLANKS = 5, STICK = 280, STAT = 16842752;
    private int action;

    public RemoteItemStack sword(B173WireClient actor) {
        return apply(actor, SWORD, new int[] {2, 5}, new int[] {8});
    }
    public RemoteItemStack shovel(B173WireClient actor) {
        return apply(actor, SHOVEL, new int[] {2}, new int[] {5, 8});
    }
    public RemoteItemStack pick(B173WireClient actor) {
        return apply(actor, PICK, new int[] {1, 2, 3}, new int[] {5, 8});
    }
    public RemoteItemStack axe(B173WireClient actor) {
        return apply(actor, AXE, new int[] {1, 2, 4}, new int[] {5, 8});
    }
    public RemoteItemStack hoe(B173WireClient actor) {
        return apply(actor, HOE, new int[] {1, 2}, new int[] {5, 8});
    }
    public int[] family(B173WireClient actor) {
        sword(actor); pick(actor); axe(actor); shovel(actor); hoe(actor);
        return new int[] {SWORD, PICK, AXE, SHOVEL, HOE};
    }

    private RemoteItemStack apply(B173WireClient actor, int resultId, int[] planks, int[] sticks) {
        try {
            RemoteItemStack result = item(resultId, 1, 0);
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
            if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || before.size() != 46
                    || !inbound.cursorObserved() || inbound.cursor() != null || result.legacyId() == PLANKS
                    || result.legacyId() == 58)
                throw new IllegalStateException("wood-tool click preflight failed");
            for (int slot = 0; slot < 10; slot++) if (!before.slot(slot).empty())
                throw new IllegalStateException("wood-tool matrix was not empty");
            int stickSlot = find(before, STICK), plankSlot = find(before, PLANKS), dest = empty(before);
            if (stickSlot < 10 || plankSlot < 10 || dest < 10)
                throw new IllegalStateException("wood-tool ingredients drifted");
            RemoteItemStack cursor = take(channel, inbound, stickSlot);
            for (int i = 0; i < sticks.length; i++)
                cursor = placeOne(channel, inbound, sticks[i], cursor, false, result);
            if (cursor != null) { put(channel, inbound, stickSlot, cursor); cursor = null; }
            cursor = take(channel, inbound, plankSlot);
            for (int i = 0; i < planks.length; i++)
                cursor = placeOne(channel, inbound, planks[i], cursor, i == planks.length - 1, result);
            if (cursor != null) put(channel, inbound, plankSlot, cursor);
            takeResult(channel, inbound, result, planks, sticks);
            store(channel, inbound, dest, result);
            if (inbound.cursor() != null) throw new IllegalStateException("wood-tool take drifted");
            return result;
        } catch (IOException error) {
            throw new IllegalStateException("wood-tool click failed", error);
        }
    }

    private RemoteItemStack take(B173PlayChannel channel, B173PlayInbound inbound, int combined)
            throws IOException {
        RemoteInventoryView before = inbound.activeWindow().inventory(), personal = inbound.inventory();
        RemoteItemStack stack = before.slot(combined).item();
        step(channel, inbound, combined, 0, stack, replace(before, combined, null), personal,
                replace(personal, combined - 1, null), stack, -1, 0);
        return stack;
    }

    private RemoteItemStack placeOne(B173PlayChannel channel, B173PlayInbound inbound, int matrix,
            RemoteItemStack cursor, boolean complete, RemoteItemStack result) throws IOException {
        RemoteInventoryView before = inbound.activeWindow().inventory(), personal = inbound.inventory();
        RemoteItemStack placed = item(cursor.legacyId(), 1, cursor.damage()), next = decr(cursor);
        RemoteInventoryView after = replace(before, matrix, placed);
        if (complete) after = replace(after, 0, result);
        step(channel, inbound, matrix, 1, null, after, personal, personal, next, -1, 0);
        return next;
    }

    private void put(B173PlayChannel channel, B173PlayInbound inbound, int combined, RemoteItemStack stack)
            throws IOException {
        RemoteInventoryView before = inbound.activeWindow().inventory(), personal = inbound.inventory();
        step(channel, inbound, combined, 0, null, replace(before, combined, stack), personal,
                replace(personal, combined - 1, stack), null, -1, 0);
    }

    private void takeResult(B173PlayChannel channel, B173PlayInbound inbound, RemoteItemStack result,
            int[] planks, int[] sticks) throws IOException {
        RemoteInventoryView before = inbound.activeWindow().inventory(), personal = inbound.inventory();
        if (before.slot(0).empty() || !before.slot(0).item().equals(result))
            throw new IllegalStateException("wood-tool result was not item " + result.legacyId());
        RemoteInventoryView taken = replace(before, 0, null);
        for (int slot : planks) taken = replace(taken, slot, null);
        for (int slot : sticks) taken = replace(taken, slot, null);
        step(channel, inbound, 0, 0, result, taken, personal, personal, result, STAT + result.legacyId(), 1);
    }

    private void store(B173PlayChannel channel, B173PlayInbound inbound, int combined, RemoteItemStack result)
            throws IOException {
        RemoteInventoryView before = inbound.activeWindow().inventory(), personal = inbound.inventory();
        step(channel, inbound, combined, 0, null, replace(before, combined, result), personal,
                replace(personal, combined - 1, result), null, -1, 0);
    }

    private void step(B173PlayChannel channel, B173PlayInbound inbound, int slot, int button,
            RemoteItemStack predicted, RemoteInventoryView after, RemoteInventoryView personalBefore,
            RemoteInventoryView personalAfter, RemoteItemStack cursorAfter, int statisticId, int increment)
            throws IOException {
        action++;
        B173ContainerStep value = statisticId < 0
                ? new B173ContainerStep(after.windowId(), action, slot, button, predicted,
                        inbound.activeWindow().inventory(), after, personalBefore, personalAfter,
                        inbound.cursor(), cursorAfter)
                : new B173ContainerStep(after.windowId(), action, slot, predicted,
                        inbound.activeWindow().inventory(), after, personalBefore, personalAfter,
                        inbound.cursor(), cursorAfter, statisticId, increment);
        inbound.beginContainerTransaction(value);
        B173ContainerPacket.write(channel.output, after.windowId(), slot, button, action, predicted);
        channel.output.flush(); inbound.awaitContainerTransaction();
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 10; slot < view.size(); slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        return -1;
    }
    private static int empty(RemoteInventoryView view) {
        for (int slot = 10; slot < view.size(); slot++) if (view.slot(slot).empty()) return slot;
        return -1;
    }
    private static RemoteItemStack item(int id, int count, int damage) {
        return new RemoteItemStack(id, count, damage);
    }
    private static RemoteItemStack decr(RemoteItemStack stack) {
        return stack.count() == 1 ? null : item(stack.legacyId(), stack.count() - 1, stack.damage());
    }
    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(source.windowId(), slots);
    }
}
