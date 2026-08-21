package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts boat 333, minecart 328, chest minecart 342, and furnace minecart 343. */
public final class B173VehicleCrafts {
    public static final int BOAT = 333, CART = 328, CHEST_CART = 342, FURNACE_CART = 343;
    private static final int PLANKS = 5, IRON = 265, CHEST = 54, FURNACE = 61, STAT = 16842752;
    private static final int[] HULL = {4, 6, 7, 9, 8};
    private final B173PlayChannel channel;
    private final B173PlayInbound inbound;
    private int windowId, action;

    private B173VehicleCrafts(B173WireClient actor) {
        channel = actor.channel();
        inbound = channel.inbound();
    }

    public static int[] apply(B173WireClient actor) {
        try {
            return new B173VehicleCrafts(actor).run();
        } catch (IOException error) {
            throw new IllegalStateException("vehicle crafts failed", error);
        }
    }

    private int[] run() throws IOException {
        if (BOAT != 333 || CART != 328 || CHEST_CART != 342 || FURNACE_CART != 343)
            throw new IllegalStateException("vehicle result identity drifted");
        RemoteContainerWindow active = inbound.activeWindow();
        if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid vehicle-craft workbench boundary");
        for (int slot = 0; slot < 10; slot++) if (!active.inventory().slot(slot).empty())
            throw new IllegalStateException("vehicle-craft matrix was not empty");
        windowId = active.inventory().windowId();
        shaped(PLANKS, BOAT);
        shaped(IRON, CART);
        shaped(IRON, CART);
        stacked(CHEST, CART, CHEST_CART);
        shaped(IRON, CART);
        stacked(FURNACE, CART, FURNACE_CART);
        if (!has(BOAT) || !has(CART) || !has(CHEST_CART) || !has(FURNACE_CART))
            throw new IllegalStateException("vehicle craft results drifted");
        return new int[] {BOAT, CART, CHEST_CART, FURNACE_CART};
    }

    private void shaped(int ingredientId, int resultId) throws IOException {
        int source = find(ingredientId);
        if (window().slot(source).item().count() < HULL.length)
            throw new IllegalStateException("vehicle hull ingredient count drifted");
        pickup(source);
        for (int index = 0; index < HULL.length; index++)
            placeOne(HULL[index], index == HULL.length - 1 ? item(resultId, 1) : null);
        if (inbound.cursor() != null) store(source, inbound.cursor());
        takeResult(item(resultId, 1));
        store(empty(), item(resultId, 1));
    }

    private void stacked(int topId, int bottomId, int resultId) throws IOException {
        pickup(find(topId));
        placeAll(2, null);
        pickup(find(bottomId));
        placeAll(5, item(resultId, 1));
        takeResult(item(resultId, 1));
        store(empty(), item(resultId, 1));
    }

    private void pickup(int combined) throws IOException {
        RemoteItemStack stack = window().slot(combined).item();
        step(combined, 0, stack, replace(window(), combined, null), replace(personal(), combined - 1, null), stack);
    }

    private void placeOne(int slot, RemoteItemStack result) throws IOException {
        RemoteItemStack cursor = inbound.cursor(), one = item(cursor.legacyId(), 1);
        RemoteInventoryView after = replace(window(), slot, one);
        if (result != null) after = replace(after, 0, result);
        step(slot, 1, null, after, personal(), dec(cursor));
    }

    private void placeAll(int slot, RemoteItemStack result) throws IOException {
        RemoteItemStack cursor = inbound.cursor();
        RemoteInventoryView after = replace(window(), slot, cursor);
        if (result != null) after = replace(after, 0, result);
        step(slot, 0, null, after, personal(), null);
    }

    private void takeResult(RemoteItemStack result) throws IOException {
        if (window().slot(0).empty() || !window().slot(0).item().equals(result) || inbound.cursor() != null)
            throw new IllegalStateException("modeled vehicle result absent");
        RemoteInventoryView taken = window();
        for (int slot = 0; slot < 10; slot++) taken = replace(taken, slot, null);
        B173ContainerStep value = new B173ContainerStep(windowId, action + 1, 0, result, window(), taken,
                personal(), personal(), inbound.cursor(), result, STAT + result.legacyId(), 1);
        send(value);
        action++;
        inbound.awaitContainerTransaction();
    }

    private void store(int combined, RemoteItemStack stack) throws IOException {
        step(combined, 0, null, replace(window(), combined, stack), replace(personal(), combined - 1, stack), null);
    }

    private void step(int slot, int button, RemoteItemStack predicted, RemoteInventoryView after,
            RemoteInventoryView personalAfter, RemoteItemStack cursorAfter) throws IOException {
        send(new B173ContainerStep(windowId, action + 1, slot, button, predicted, window(), after, personal(),
                personalAfter, inbound.cursor(), cursorAfter));
        action++;
        inbound.awaitContainerTransaction();
    }

    private void send(B173ContainerStep value) throws IOException {
        inbound.beginContainerTransaction(value);
        B173ContainerPacket.write(channel.output, windowId, value.slot, value.button, value.action, value.predicted);
        channel.output.flush();
    }

    private RemoteInventoryView window() { return inbound.activeWindow().inventory(); }
    private RemoteInventoryView personal() { return inbound.inventory(); }

    private int find(int id) {
        RemoteInventoryView view = window();
        for (int slot = 10; slot < view.size(); slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        throw new IllegalStateException("vehicle ingredient " + id + " absent");
    }

    private int empty() {
        RemoteInventoryView view = window();
        for (int slot = 10; slot < view.size(); slot++) if (view.slot(slot).empty()) return slot;
        throw new IllegalStateException("vehicle destination absent");
    }

    private boolean has(int id) {
        RemoteInventoryView view = personal();
        for (int slot = 9; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return true;
        return false;
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(source.windowId(), slots);
    }

    private static RemoteItemStack item(int id, int count) { return new RemoteItemStack(id, count, 0); }

    private static RemoteItemStack dec(RemoteItemStack stack) {
        return stack.count() == 1 ? null : item(stack.legacyId(), stack.count() - 1);
    }
}
