package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench 3x3 Packet102 sequence that crafts the iron armor family from ingots 265. */
public final class B173ArmorCraftsClick {
    public static final int INGOT = 265;
    public static final int[] RESULTS = {306, 307, 308, 309};
    private static final int[][] GRIDS = {
            {4, 6, 1, 2, 3}, {5, 4, 6, 7, 8, 9, 1, 3}, {7, 2, 1, 3, 4, 6, 9}, {1, 3, 4, 6}};
    private B173ArmorCraftsClick() {}

    public static int[] apply(B173WireClient actor, int ironSlot, int[] dests) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            RemoteContainerWindow window = inbound.activeWindow();
            if (window.descriptor().kind() != RemoteWindowKind.WORKBENCH || window.inventory().size() != 46
                    || dests == null || dests.length != 4 || ironSlot < 9 || ironSlot > 44)
                throw new IllegalStateException("armor craft window drifted");
            for (int slot = 0; slot < 10; slot++) if (!window.inventory().slot(slot).empty())
                throw new IllegalStateException("armor craft matrix was not empty");
            int windowId = window.descriptor().windowId(), action = 0;
            int[] crafted = new int[4];
            for (int index = 0; index < 4; index++) {
                requireDest(inbound.inventory(), dests[index], ironSlot);
                action = craft(channel, inbound, windowId, action, ironSlot, dests[index], GRIDS[index],
                        RESULTS[index]);
                crafted[index] = RESULTS[index];
            }
            RemoteInventoryView personal = inbound.inventory();
            if (!personal.slot(5).empty() || !personal.slot(6).empty() || !personal.slot(7).empty()
                    || !personal.slot(8).empty())
                throw new IllegalStateException("armor craft equipped like M270-M273");
            return crafted;
        } catch (IOException error) {
            throw new IllegalStateException("armor craft failed", error);
        }
    }

    private static int craft(B173PlayChannel channel, B173PlayInbound inbound, int windowId, int action,
            int ironSlot, int dest, int[] grid, int resultId) throws IOException {
        RemoteItemStack iron = inbound.inventory().slot(ironSlot).item();
        if (iron.legacyId() != INGOT || iron.count() < grid.length)
            throw new IllegalStateException("iron ingot seed drifted");
        int combined = ironSlot + 1;
        action = click(channel, inbound, windowId, action, combined, 0, iron,
                replace(inbound.activeWindow().inventory(), combined, null),
                replace(inbound.inventory(), ironSlot, null), iron);
        for (int index = 0; index < grid.length; index++) {
            RemoteInventoryView after = replace(inbound.activeWindow().inventory(), grid[index], item(INGOT, 1));
            if (index == grid.length - 1) after = replace(after, 0, item(resultId, 1));
            action = click(channel, inbound, windowId, action, grid[index], 1, null, after,
                    inbound.inventory(), dec(inbound.cursor()));
        }
        if (inbound.cursor() != null) {
            RemoteItemStack leftover = inbound.cursor();
            action = click(channel, inbound, windowId, action, combined, 0, null,
                    replace(inbound.activeWindow().inventory(), combined, leftover),
                    replace(inbound.inventory(), ironSlot, leftover), null);
        }
        RemoteItemStack result = item(resultId, 1); RemoteInventoryView cleared = inbound.activeWindow().inventory();
        for (int slot = 0; slot <= 9; slot++) cleared = replace(cleared, slot, null);
        action = click(channel, inbound, windowId, action, 0, 0, result, cleared, inbound.inventory(), result);
        int stored = dest + 1;
        return click(channel, inbound, windowId, action, stored, 0, null,
                replace(inbound.activeWindow().inventory(), stored, result),
                replace(inbound.inventory(), dest, result), null);
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

    private static void requireDest(RemoteInventoryView personal, int dest, int ironSlot) {
        if (dest < 9 || dest > 44 || dest == ironSlot || dest == 5 || dest == 6 || dest == 7 || dest == 8
                || !personal.slot(dest).empty())
            throw new IllegalStateException("armor craft destination drifted");
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(source.windowId(), slots);
    }

    private static RemoteItemStack dec(RemoteItemStack stack) {
        if (stack == null || stack.legacyId() != INGOT)
            throw new IllegalStateException("armor craft cursor drifted");
        return stack.count() == 1 ? null : item(INGOT, stack.count() - 1);
    }

    private static RemoteItemStack item(int id, int count) { return new RemoteItemStack(id, count, 0); }
}
