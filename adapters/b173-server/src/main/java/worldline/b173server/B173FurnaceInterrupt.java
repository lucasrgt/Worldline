package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Reusable Packet102 take/store contract for furnace input and fuel slots. */
public final class B173FurnaceInterrupt {
    private static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0);
    private static final RemoteItemStack COAL = new RemoteItemStack(263, 1, 0);

    private B173FurnaceInterrupt() {}

    public static RemoteContainerWindow active(B173WireClient actor) {
        return actor.channel().inbound().activeWindow();
    }

    public static RemoteInventoryView window(B173WireClient actor) {
        return active(actor).inventory();
    }

    public static void pump(B173WireClient actor) throws IOException {
        actor.channel().inbound().pumpAvailable();
    }

    public static int store(B173WireClient actor, int personalSlot, int ownedSlot, int action)
            throws IOException {
        pump(actor);
        return move(actor.channel(), actor.channel().inbound(), personalSlot, ownedSlot, action);
    }

    public static int take(B173WireClient actor, int ownedSlot, int personalSlot, int action)
            throws IOException {
        pump(actor);
        B173PlayChannel channel = actor.channel();
        B173PlayInbound inbound = channel.inbound();
        RemoteContainerWindow active = inbound.activeWindow();
        RemoteInventoryView before = active.inventory();
        int combined = active.descriptor().playerTailOffset() + personalSlot - 9;
        require(furnace(active) && ownedSlot >= 0 && ownedSlot <= 1 && personalSlot >= 9
                && personalSlot <= 44 && !before.slot(ownedSlot).empty()
                && before.slot(combined).empty() && inbound.inventory().slot(personalSlot).empty()
                && inbound.cursorObserved() && inbound.cursor() == null,
                "furnace take requires occupied owned slot and empty personal slot");
        RemoteItemStack stack = before.slot(ownedSlot).item();
        requireAllowed(stack);
        RemoteInventoryView personal = inbound.inventory();
        RemoteInventoryView taken = replace(before, ownedSlot, null);
        step(channel, inbound, ownedSlot, stack, taken, personal, personal, stack, action++);
        RemoteInventoryView stored = replace(inbound.activeWindow().inventory(), combined, stack);
        RemoteInventoryView personalStored = replace(personal, personalSlot, stack);
        step(channel, inbound, combined, null, stored, personal, personalStored, null, action++);
        require(inbound.cursor() == null && inbound.inventory().slot(personalSlot).item().equals(stack)
                && inbound.activeWindow().inventory().slot(ownedSlot).empty(),
                "furnace take drifted");
        return action;
    }

    private static int move(B173PlayChannel channel, B173PlayInbound inbound, int personalSlot,
            int ownedSlot, int action) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow();
        RemoteInventoryView before = active.inventory();
        int combined = active.descriptor().playerTailOffset() + personalSlot - 9;
        require(furnace(active) && ownedSlot >= 0 && ownedSlot <= 1 && personalSlot >= 9
                && personalSlot <= 44 && !before.slot(combined).empty()
                && before.slot(ownedSlot).empty() && inbound.cursorObserved()
                && inbound.cursor() == null, "furnace store requires occupied personal source");
        RemoteItemStack stack = before.slot(combined).item();
        requireAllowed(stack);
        RemoteInventoryView personal = inbound.inventory();
        RemoteInventoryView taken = replace(before, combined, null);
        RemoteInventoryView personalTaken = replace(personal, personalSlot, null);
        step(channel, inbound, combined, stack, taken, personal, personalTaken, stack, action++);
        step(channel, inbound, ownedSlot, null,
                replace(inbound.activeWindow().inventory(), ownedSlot, stack),
                personalTaken, personalTaken, null, action++);
        require(inbound.cursor() == null && inbound.inventory().slot(personalSlot).empty()
                && inbound.activeWindow().inventory().slot(ownedSlot).item().equals(stack),
                "furnace store drifted");
        return action;
    }

    private static void step(B173PlayChannel channel, B173PlayInbound inbound, int slot,
            RemoteItemStack predicted, RemoteInventoryView after, RemoteInventoryView personalBefore,
            RemoteInventoryView personalAfter, RemoteItemStack cursorAfter, int action)
            throws IOException {
        require(action >= 1 && action <= 32767, "furnace interrupt action exhausted");
        RemoteContainerWindow active = inbound.activeWindow();
        B173ContainerStep step = new B173ContainerStep(active.descriptor().windowId(), action, slot,
                predicted, active.inventory(), after, personalBefore, personalAfter,
                inbound.cursor(), cursorAfter);
        inbound.beginContainerTransaction(step);
        B173ContainerPacket.write(channel.output, step.windowId, slot, 0, action, predicted);
        channel.output.flush();
        inbound.awaitContainerTransaction();
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot,
            RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(source.windowId(), slots);
    }

    private static boolean furnace(RemoteContainerWindow active) {
        return active.descriptor().kind() == RemoteWindowKind.FURNACE
                && active.descriptor().containerSlots() == 3
                && "Furnace".equals(active.descriptor().title())
                && active.inventory().size() == 39;
    }

    private static void requireAllowed(RemoteItemStack stack) {
        require(stack.equals(COBBLE) || stack.equals(COAL), "furnace interrupt item drifted");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
