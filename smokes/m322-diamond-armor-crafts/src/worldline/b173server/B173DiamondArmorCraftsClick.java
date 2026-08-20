package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench 3x3 Packet102 sequence that crafts the diamond armor family from gems 264. */
public final class B173DiamondArmorCraftsClick {
    public static final int DIAMOND = 264;
    public static final int[] RESULTS = {310, 311, 312, 313};
    private static final int[][] GRIDS = {
            {4, 6, 1, 2, 3}, {5, 4, 6, 7, 8, 9, 1, 3}, {7, 2, 1, 3, 4, 6, 9}, {1, 3, 4, 6}};
    private B173DiamondArmorCraftsClick() {}

    public static int[] apply(B173WireClient actor, int diamondSlot, int[] dests) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            RemoteContainerWindow window = inbound.activeWindow();
            if (window.descriptor().kind() != RemoteWindowKind.WORKBENCH || window.inventory().size() != 46
                    || dests == null || dests.length != 4 || diamondSlot < 9 || diamondSlot > 44)
                throw new IllegalStateException("diamond armor craft window drifted");
            for (int slot = 0; slot < 10; slot++) if (!window.inventory().slot(slot).empty())
                throw new IllegalStateException("diamond armor craft matrix was not empty");
            int windowId = window.descriptor().windowId(), action = 0;
            int[] crafted = new int[4];
            for (int index = 0; index < 4; index++) {
                requireDest(inbound.inventory(), dests[index], diamondSlot);
                action = craft(channel, inbound, windowId, action, diamondSlot, dests[index], GRIDS[index],
                        RESULTS[index]);
                crafted[index] = RESULTS[index];
            }
            RemoteInventoryView personal = inbound.inventory();
            if (!personal.slot(5).empty() || !personal.slot(6).empty() || !personal.slot(7).empty()
                    || !personal.slot(8).empty())
                throw new IllegalStateException("diamond armor craft equipped like M272");
            return crafted;
        } catch (IOException error) {
            throw new IllegalStateException("diamond armor craft failed", error);
        }
    }

    private static int craft(B173PlayChannel channel, B173PlayInbound inbound, int windowId, int action,
            int diamondSlot, int dest, int[] grid, int resultId) throws IOException {
        RemoteItemStack diamond = inbound.inventory().slot(diamondSlot).item();
        if (diamond.legacyId() != DIAMOND || diamond.count() < grid.length)
            throw new IllegalStateException("diamond seed drifted");
        int combined = diamondSlot + 1;
        action = click(channel, inbound, windowId, action, combined, 0, diamond,
                replace(inbound.activeWindow().inventory(), combined, null),
                replace(inbound.inventory(), diamondSlot, null), diamond);
        for (int index = 0; index < grid.length; index++) {
            RemoteInventoryView after = replace(inbound.activeWindow().inventory(), grid[index], item(DIAMOND, 1));
            if (index == grid.length - 1) after = replace(after, 0, item(resultId, 1));
            action = click(channel, inbound, windowId, action, grid[index], 1, null, after,
                    inbound.inventory(), dec(inbound.cursor()));
        }
        if (inbound.cursor() != null) {
            RemoteItemStack leftover = inbound.cursor();
            action = click(channel, inbound, windowId, action, combined, 0, null,
                    replace(inbound.activeWindow().inventory(), combined, leftover),
                    replace(inbound.inventory(), diamondSlot, leftover), null);
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

    private static void requireDest(RemoteInventoryView personal, int dest, int diamondSlot) {
        if (dest < 9 || dest > 44 || dest == diamondSlot || dest == 5 || dest == 6 || dest == 7 || dest == 8
                || !personal.slot(dest).empty())
            throw new IllegalStateException("diamond armor craft destination drifted");
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(source.windowId(), slots);
    }

    private static RemoteItemStack dec(RemoteItemStack stack) {
        if (stack == null || stack.legacyId() != DIAMOND)
            throw new IllegalStateException("diamond armor craft cursor drifted");
        return stack.count() == 1 ? null : item(DIAMOND, stack.count() - 1);
    }

    private static RemoteItemStack item(int id, int count) { return new RemoteItemStack(id, count, 0); }
}
