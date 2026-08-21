package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts painting 321, sign 323, and bowl 281x4. */
public final class B173PlaceableWorkbenchCrafts {
    public static final RemoteItemStack PAINTING = new RemoteItemStack(321, 1, 0);
    public static final RemoteItemStack SIGN = new RemoteItemStack(323, 1, 0);
    public static final RemoteItemStack BOWL = new RemoteItemStack(281, 4, 0);
    private static final int[] RING = {1, 2, 3, 4, 6, 7, 8, 9}, SIGN_PLANKS = {1, 2, 3, 4, 5, 6};
    private static final int[] BOWL_CELLS = {1, 3, 5};
    private final B173PlayChannel channel; private final B173PlayInbound inbound;
    private int windowId, action; private long epoch = -1L;

    private B173PlaceableWorkbenchCrafts(B173WireClient actor) {
        channel = actor.channel(); inbound = channel.inbound(); }

    public static void apply(B173WireClient actor) {
        try { new B173PlaceableWorkbenchCrafts(actor).run(); }
        catch (IOException error) { throw new IllegalStateException("placeable workbench crafts failed", error); }
    }

    public static boolean stored(RemoteInventoryView view) {
        return same(view, 38, BOWL) && same(view, 39, PAINTING) && same(view, 40, SIGN)
                && view.slot(37).empty() && view.occupiedSlots() == 4;
    }

    private void run() throws IOException {
        require(PAINTING.legacyId() == 321 && SIGN.legacyId() == 323 && BOWL.legacyId() == 281
                && BOWL.count() == 4, "placeable result identity drifted");
        RemoteContainerWindow active = inbound.activeWindow();
        if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
                || !inbound.cursorObserved() || inbound.cursor() != null
                || !B173PlaceablePersonalCrafts.prepared(inbound.inventory()))
            throw new IllegalStateException("invalid placeable workbench boundary");
        fill(39, 35, new int[] {5}, null);
        fill(37, 280, RING, PAINTING);
        finish(39, PAINTING);
        fill(40, 280, new int[] {8}, null);
        fill(38, 5, SIGN_PLANKS, SIGN);
        finish(40, SIGN);
        fill(38, 5, BOWL_CELLS, BOWL);
        finish(38, BOWL);
        if (!stored(inbound.inventory()) || inbound.cursor() != null)
            throw new IllegalStateException("placeable workbench results drifted");
    }

    private void fill(int personal, int id, int[] cells, RemoteItemStack complete) throws IOException {
        pickup(personal, id, cells.length);
        for (int index = 0; index < cells.length; index++)
            placeOne(cells[index], index == cells.length - 1 ? complete : null);
        storeLeftover(personal);
    }

    private void finish(int dest, RemoteItemStack result) throws IOException {
        takeResult(result, clearOwned(window()), 16842752 + result.legacyId());
        int combined = dest + 1;
        step(combined, 0, null, replace(window(), combined, result), personal(),
                replace(personal(), dest, result), null);
    }

    private void pickup(int personal, int expectedId, int needed) throws IOException {
        int combined = personal + 1; RemoteInventoryView before = window();
        if (before.slot(combined).empty() || inbound.cursor() != null)
            throw new IllegalStateException("placeable ingredient preflight failed");
        RemoteItemStack stack = before.slot(combined).item();
        if (stack.legacyId() != expectedId || stack.count() < needed || stack.damage() != 0
                || !personal().slot(personal).item().equals(stack))
            throw new IllegalStateException("placeable ingredient seed drifted");
        step(combined, 0, stack, replace(before, combined, null), personal(),
                replace(personal(), personal, null), stack);
    }

    private void placeOne(int cell, RemoteItemStack result) throws IOException {
        RemoteItemStack cursor = inbound.cursor();
        if (cursor == null || !window().slot(cell).empty())
            throw new IllegalStateException("placeable matrix place drifted");
        RemoteItemStack one = item(cursor.legacyId(), 1, cursor.damage());
        RemoteInventoryView after = replace(window(), cell, one);
        if (result != null) after = replace(after, 0, result);
        step(cell, 1, null, after, personal(), personal(), dec(cursor));
    }

    private void storeLeftover(int personal) throws IOException {
        RemoteItemStack cursor = inbound.cursor(); if (cursor == null) return;
        int combined = personal + 1;
        if (!window().slot(combined).empty() || !personal().slot(personal).empty())
            throw new IllegalStateException("placeable leftover store drifted");
        step(combined, 0, null, replace(window(), combined, cursor), personal(),
                replace(personal(), personal, cursor), null);
    }

    private void takeResult(RemoteItemStack result, RemoteInventoryView taken, int statisticId)
            throws IOException {
        if (window().slot(0).empty() || !window().slot(0).item().equals(result) || inbound.cursor() != null)
            throw new IllegalStateException("placeable result " + result.legacyId() + " absent");
        send(new B173ContainerStep(windowId(), action + 1, 0, result, window(), taken,
                personal(), personal(), inbound.cursor(), result, statisticId, result.count()));
        action++; inbound.awaitContainerTransaction();
    }

    private void step(int slot, int button, RemoteItemStack predicted, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter,
            RemoteItemStack cursorAfter) throws IOException {
        send(new B173ContainerStep(windowId(), action + 1, slot, button, predicted, window(), after,
                personalBefore, personalAfter, inbound.cursor(), cursorAfter)); action++;
        inbound.awaitContainerTransaction();
    }

    private void send(B173ContainerStep value) throws IOException {
        inbound.beginContainerTransaction(value);
        B173ContainerPacket.write(channel.output, windowId(), value.slot, value.button, value.action,
                value.predicted);
        channel.output.flush();
    }

    private int windowId() {
        long current = inbound.activeWindowEpoch();
        if (epoch != current) { epoch = current; windowId = window().windowId(); action = 0; }
        return windowId;
    }

    private RemoteInventoryView window() { return inbound.activeWindow().inventory(); }
    private RemoteInventoryView personal() { return inbound.inventory(); }

    private static RemoteInventoryView clearOwned(RemoteInventoryView view) {
        RemoteInventoryView next = view; for (int slot = 0; slot < 10; slot++) next = replace(next, slot, null);
        return next; }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(source.windowId(), slots); }

    private static RemoteItemStack item(int id, int count, int damage) {
        return new RemoteItemStack(id, count, damage); }

    private static RemoteItemStack dec(RemoteItemStack stack) {
        return stack.count() == 1 ? null : new RemoteItemStack(stack.legacyId(), stack.count() - 1, stack.damage()); }

    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item); }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
}
