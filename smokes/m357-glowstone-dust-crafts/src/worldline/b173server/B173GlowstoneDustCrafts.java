package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 2x2: four glowstone dust 348 become one glowstone block 89. */
public final class B173GlowstoneDustCrafts {
    public static final RemoteItemStack DUST4 = new RemoteItemStack(348, 4, 0);
    public static final RemoteItemStack DUST1 = new RemoteItemStack(348, 1, 0);
    public static final RemoteItemStack GLOWSTONE = new RemoteItemStack(89, 1, 0);
    private static final int[] CELLS = {1, 2, 3, 4};
    private B173GlowstoneDustCrafts() {}

    public static void apply(B173WireClient actor) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            RemoteInventoryView before = inbound.inventory();
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
                    || before.windowId() != 0 || before.size() != 45 || !emptyCraft(before)
                    || !same(before, 36, DUST4) || DUST4.legacyId() != 348 || GLOWSTONE.legacyId() != 89)
                throw new IllegalStateException("glowstone dust 2x2 preflight failed");
            step(channel, inbound, 1, 36, 0, DUST4, replace(before, 36, null), DUST4);
            RemoteItemStack cursor = DUST4;
            for (int index = 0; index < CELLS.length; index++) {
                cursor = dec(cursor);
                RemoteInventoryView after = replace(inbound.inventory(), CELLS[index], DUST1);
                if (index == 3) after = replace(after, 0, GLOWSTONE);
                step(channel, inbound, index + 2, CELLS[index], 1, null, after, cursor);
            }
            if (!same(inbound.inventory(), 0, GLOWSTONE) || inbound.inventory().slot(0).item().legacyId() != 89)
                throw new IllegalStateException("glowstone result 89 absent from 2x2 dust");
            step(channel, inbound, 6, 0, 0, GLOWSTONE, clearCraft(inbound.inventory()), GLOWSTONE);
            step(channel, inbound, 7, 36, 0, null, replace(inbound.inventory(), 36, GLOWSTONE), null);
            if (!same(inbound.inventory(), 36, GLOWSTONE) || !emptyCraft(inbound.inventory())
                    || inbound.cursor() != null)
                throw new IllegalStateException("glowstone 89 store drifted");
        } catch (IOException error) {
            throw new IllegalStateException("glowstone dust 2x2 click failed", error);
        }
    }

    private static void step(B173PlayChannel channel, B173PlayInbound inbound, int action, int slot, int button,
            RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter) throws IOException {
        inbound.beginPersonalTransaction(action, slot, predicted, inbound.inventory(), after,
                inbound.cursor(), cursorAfter);
        B173ContainerPacket.write(channel.output, 0, slot, button, action, predicted);
        channel.output.flush(); inbound.awaitPersonalStep();
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(0, slots);
    }

    private static RemoteInventoryView clearCraft(RemoteInventoryView source) {
        RemoteInventoryView next = source;
        for (int slot = 0; slot < 5; slot++) next = replace(next, slot, null);
        return next;
    }

    private static RemoteItemStack dec(RemoteItemStack stack) {
        return stack.count() == 1 ? null : new RemoteItemStack(stack.legacyId(), stack.count() - 1, stack.damage());
    }

    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
    }

    public static boolean emptyCraft(RemoteInventoryView view) {
        for (int slot = 0; slot < 5; slot++) if (!view.slot(slot).empty()) return false;
        return true;
    }
}
