package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts empty bucket 325 from three iron ingots 265. */
public final class B173UtilityWorkbenchCrafts {
    public static final RemoteItemStack BUCKET = new RemoteItemStack(325, 1, 0);
    private static final int IRON = 265, STAT = 16842752;
    private B173UtilityWorkbenchCrafts() {}

    public static void apply(B173WireClient actor) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            RemoteContainerWindow window = inbound.activeWindow();
            if (window.descriptor().kind() != RemoteWindowKind.WORKBENCH || window.inventory().size() != 46
                    || !inbound.cursorObserved() || inbound.cursor() != null
                    || !same(inbound.inventory(), 38, item(3)) || !B173UtilityPersonalCrafts.stored(inbound.inventory())
                    || BUCKET.legacyId() != 325)
                throw new IllegalStateException("utility bucket preflight failed");
            for (int slot = 0; slot < 10; slot++) if (!window.inventory().slot(slot).empty())
                throw new IllegalStateException("utility bucket matrix was not empty");
            int windowId = window.descriptor().windowId(), action = 0;
            RemoteItemStack iron = item(3);
            action = click(channel, inbound, windowId, action, 39, 0, iron,
                    replace(inbound.activeWindow().inventory(), 39, null),
                    replace(inbound.inventory(), 38, null), iron);
            int[] cells = {1, 3, 5};
            for (int index = 0; index < cells.length; index++) {
                RemoteInventoryView after = replace(inbound.activeWindow().inventory(), cells[index], item(1));
                if (index == cells.length - 1) after = replace(after, 0, BUCKET);
                action = click(channel, inbound, windowId, action, cells[index], 1, null, after,
                        inbound.inventory(), dec(inbound.cursor()));
            }
            RemoteInventoryView cleared = inbound.activeWindow().inventory();
            for (int slot = 0; slot <= 9; slot++) cleared = replace(cleared, slot, null);
            action = takeResult(channel, inbound, windowId, action, cleared);
            action = click(channel, inbound, windowId, action, 41, 0, null,
                    replace(inbound.activeWindow().inventory(), 41, BUCKET),
                    replace(inbound.inventory(), 40, BUCKET), null);
            if (action < 1 || !stored(inbound.inventory()) || inbound.cursor() != null)
                throw new IllegalStateException("utility bucket result drifted");
        } catch (IOException error) {
            throw new IllegalStateException("utility bucket craft failed", error);
        }
    }

    public static boolean stored(RemoteInventoryView view) {
        return B173UtilityPersonalCrafts.stored(view) && same(view, 40, BUCKET) && view.slot(38).empty()
                && view.occupiedSlots() == 4;
    }

    private static int click(B173PlayChannel channel, B173PlayInbound inbound, int windowId, int action,
            int slot, int button, RemoteItemStack predicted, RemoteInventoryView after,
            RemoteInventoryView personalAfter, RemoteItemStack cursorAfter) throws IOException {
        int next = action + 1;
        inbound.beginContainerTransaction(new B173ContainerStep(windowId, next, slot, button, predicted,
                inbound.activeWindow().inventory(), after, inbound.inventory(), personalAfter,
                inbound.cursor(), cursorAfter));
        B173ContainerPacket.write(channel.output, windowId, slot, button, next, predicted);
        channel.output.flush(); inbound.awaitContainerTransaction(); return next;
    }

    private static int takeResult(B173PlayChannel channel, B173PlayInbound inbound, int windowId, int action,
            RemoteInventoryView cleared) throws IOException {
        int next = action + 1;
        inbound.beginContainerTransaction(new B173ContainerStep(windowId, next, 0, BUCKET,
                inbound.activeWindow().inventory(), cleared, inbound.inventory(), inbound.inventory(),
                inbound.cursor(), BUCKET, STAT + BUCKET.legacyId(), 1));
        B173ContainerPacket.write(channel.output, windowId, 0, 0, next, BUCKET);
        channel.output.flush(); inbound.awaitContainerTransaction(); return next;
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(source.windowId(), slots);
    }

    private static RemoteItemStack item(int count) { return new RemoteItemStack(IRON, count, 0); }
    private static RemoteItemStack dec(RemoteItemStack stack) {
        if (stack == null || stack.legacyId() != IRON) throw new IllegalStateException("utility iron cursor drifted");
        return stack.count() == 1 ? null : item(stack.count() - 1); }
    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item); }
}
