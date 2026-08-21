package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts compass 345, clock 347, and empty map 358. */
public final class B173NavigationCrafts {
    public static final RemoteItemStack COMPASS = new RemoteItemStack(345, 1, 0);
    public static final RemoteItemStack CLOCK = new RemoteItemStack(347, 1, 0);
    public static final RemoteItemStack MAP = new RemoteItemStack(358, 1, 0);
    private static final int[] PLUS = {2, 4, 6, 8}, RING = {1, 2, 3, 4, 6, 7, 8, 9};
    private final B173PlayChannel channel; private final B173PlayInbound inbound;
    private int windowId, action; private long epoch = -1L;

    private B173NavigationCrafts(B173WireClient actor) {
        channel = actor.channel(); inbound = channel.inbound(); }

    public static void apply(B173WireClient actor) {
        try { new B173NavigationCrafts(actor).run(); }
        catch (IOException error) { throw new IllegalStateException("navigation crafts failed", error); }
    }

    private void run() throws IOException {
        require(COMPASS.legacyId() == 345 && CLOCK.legacyId() == 347 && MAP.legacyId() == 358,
                "navigation result identity drifted");
        RemoteContainerWindow active = inbound.activeWindow();
        if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid navigation workbench boundary");
        craft(40, 331, 38, 265, 37, PLUS, COMPASS);
        craft(40, 331, 39, 266, 39, PLUS, CLOCK);
        craft(40, 331, 38, 265, 38, PLUS, COMPASS);
        craft(38, 345, 41, 339, 41, RING, MAP);
        require(!personal().slot(37).empty() && personal().slot(37).item().equals(COMPASS)
                && !personal().slot(39).empty() && personal().slot(39).item().equals(CLOCK)
                && !personal().slot(41).empty() && personal().slot(41).item().equals(MAP)
                && inbound.cursor() == null, "navigation crafted inventory drifted");
    }

    private void craft(int centerPersonal, int centerId, int ringPersonal, int ringId, int destPersonal,
            int[] ring, RemoteItemStack result) throws IOException {
        pickup(centerPersonal, centerId, 1); placeOne(5, null); storeLeftover(centerPersonal);
        pickup(ringPersonal, ringId, ring.length);
        for (int index = 0; index < ring.length; index++)
            placeOne(ring[index], index == ring.length - 1 ? result : null);
        storeLeftover(ringPersonal);
        takeResult(result, clearOwned(window()), 16842752 + result.legacyId());
        int combined = destPersonal + 1;
        step(combined, 0, null, replace(window(), combined, result), personal(),
                replace(personal(), destPersonal, result), null);
    }

    private void pickup(int personal, int expectedId, int needed) throws IOException {
        int combined = personal + 1; RemoteInventoryView before = window();
        if (before.slot(combined).empty() || inbound.cursor() != null)
            throw new IllegalStateException("navigation ingredient preflight failed");
        RemoteItemStack stack = before.slot(combined).item();
        if (stack.legacyId() != expectedId || stack.count() < needed || stack.damage() != 0
                || !personal().slot(personal).item().equals(stack))
            throw new IllegalStateException("navigation ingredient seed drifted");
        step(combined, 0, stack, replace(before, combined, null), personal(),
                replace(personal(), personal, null), stack);
    }

    private void placeOne(int cell, RemoteItemStack result) throws IOException {
        RemoteItemStack cursor = inbound.cursor();
        if (cursor == null || !window().slot(cell).empty())
            throw new IllegalStateException("navigation matrix place drifted");
        RemoteItemStack one = item(cursor.legacyId(), 1, cursor.damage());
        RemoteInventoryView after = replace(window(), cell, one);
        if (result != null) after = replace(after, 0, result);
        step(cell, 1, null, after, personal(), personal(), dec(cursor));
    }

    private void storeLeftover(int personal) throws IOException {
        RemoteItemStack cursor = inbound.cursor(); if (cursor == null) return;
        int combined = personal + 1;
        if (!window().slot(combined).empty() || !personal().slot(personal).empty())
            throw new IllegalStateException("navigation leftover store drifted");
        step(combined, 0, null, replace(window(), combined, cursor), personal(),
                replace(personal(), personal, cursor), null);
    }

    private void takeResult(RemoteItemStack result, RemoteInventoryView taken, int statisticId)
            throws IOException {
        if (window().slot(0).empty() || !window().slot(0).item().equals(result) || inbound.cursor() != null)
            throw new IllegalStateException("navigation result absent");
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

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
}
