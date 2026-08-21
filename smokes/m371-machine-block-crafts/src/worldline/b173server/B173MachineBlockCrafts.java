package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts TNT 46, piston 33, and sticky piston 29. */
public final class B173MachineBlockCrafts {
    public static final int TNT = 46, PISTON = 33, STICKY = 29;
    private static final int SAND = 12, GUNPOWDER = 289, PLANKS = 5, COBBLE = 4, IRON = 265, REDSTONE = 331, SLIME = 341, STAT = 16842752;
    private static final RemoteItemStack PLATE = item(72, 1), SLAB = item(44, 3, 2);
    private final B173PlayChannel channel; private final B173PlayInbound inbound; private int windowId, action;

    private B173MachineBlockCrafts(B173WireClient actor) {
        channel = actor.channel(); inbound = channel.inbound(); }

    public static int[] apply(B173WireClient actor) {
        try { return new B173MachineBlockCrafts(actor).run(); }
        catch (IOException error) { throw new IllegalStateException("machine-block crafts failed", error); }
    }

    private int[] run() throws IOException {
        if (TNT != 46 || PISTON != 33 || STICKY != 29)
            throw new IllegalStateException("machine-block result identity drifted");
        RemoteContainerWindow active = inbound.activeWindow();
        if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid machine-block workbench boundary");
        for (int slot = 0; slot < 10; slot++) if (!active.inventory().slot(slot).empty())
            throw new IllegalStateException("machine-block matrix was not empty");
        windowId = active.inventory().windowId();
        spread(GUNPOWDER, new int[] {1, 3, 5, 7, 9}, none(5));
        spread(SAND, new int[] {2, 4, 6, 8}, last(4, item(TNT, 1)));
        finish(item(TNT, 1));
        piston(); piston();
        stacked(SLIME, PISTON, STICKY);
        if (!has(TNT) || !has(PISTON) || !has(STICKY))
            throw new IllegalStateException("machine-block craft results drifted");
        return new int[] {TNT, PISTON, STICKY};
    }

    private void piston() throws IOException {
        spread(PLANKS, new int[] {1, 2, 3}, new RemoteItemStack[] {null, PLATE, SLAB});
        spread(COBBLE, new int[] {4, 6, 7, 9}, none(4));
        spread(IRON, new int[] {5}, none(1));
        spread(REDSTONE, new int[] {8}, last(1, item(PISTON, 1)));
        finish(item(PISTON, 1));
    }

    private void stacked(int topId, int bottomId, int resultId) throws IOException {
        pickup(find(topId)); placeAll(2, null); pickup(find(bottomId)); placeAll(5, item(resultId, 1));
        finish(item(resultId, 1));
    }

    private void spread(int ingredientId, int[] cells, RemoteItemStack[] mid) throws IOException {
        int source = find(ingredientId);
        if (window().slot(source).item().count() < cells.length)
            throw new IllegalStateException("machine-block ingredient count drifted");
        pickup(source);
        for (int index = 0; index < cells.length; index++) placeOne(cells[index], mid[index]);
        if (inbound.cursor() != null) store(source, inbound.cursor());
    }

    private void finish(RemoteItemStack result) throws IOException {
        takeResult(result); store(empty(), result);
    }

    private void pickup(int combined) throws IOException {
        RemoteItemStack stack = window().slot(combined).item();
        step(combined, 0, stack, replace(window(), combined, null), replace(personal(), combined - 1, null), stack);
    }

    private void placeOne(int slot, RemoteItemStack result) throws IOException {
        RemoteItemStack cursor = inbound.cursor(), one = item(cursor.legacyId(), 1);
        step(slot, 1, null, replace(replace(window(), slot, one), 0, result), personal(), dec(cursor));
    }

    private void placeAll(int slot, RemoteItemStack result) throws IOException {
        RemoteItemStack cursor = inbound.cursor();
        step(slot, 0, null, replace(replace(window(), slot, cursor), 0, result), personal(), null);
    }

    private void takeResult(RemoteItemStack result) throws IOException {
        if (window().slot(0).empty() || !window().slot(0).item().equals(result) || inbound.cursor() != null)
            throw new IllegalStateException("modeled machine-block result absent");
        RemoteInventoryView taken = window();
        for (int slot = 0; slot < 10; slot++) taken = replace(taken, slot, null);
        send(new B173ContainerStep(windowId, action + 1, 0, result, window(), taken, personal(), personal(),
                inbound.cursor(), result, STAT + result.legacyId(), 1));
        action++; inbound.awaitContainerTransaction();
    }

    private void store(int combined, RemoteItemStack stack) throws IOException {
        step(combined, 0, null, replace(window(), combined, stack), replace(personal(), combined - 1, stack), null);
    }

    private void step(int slot, int button, RemoteItemStack predicted, RemoteInventoryView after,
            RemoteInventoryView personalAfter, RemoteItemStack cursorAfter) throws IOException {
        send(new B173ContainerStep(windowId, action + 1, slot, button, predicted, window(), after, personal(),
                personalAfter, inbound.cursor(), cursorAfter));
        action++; inbound.awaitContainerTransaction();
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
        throw new IllegalStateException("machine-block ingredient " + id + " absent");
    }

    private int empty() {
        RemoteInventoryView view = window();
        for (int slot = 10; slot < view.size(); slot++) if (view.slot(slot).empty()) return slot;
        throw new IllegalStateException("machine-block destination absent");
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
    private static RemoteItemStack item(int id, int count, int damage) { return new RemoteItemStack(id, count, damage); }
    private static RemoteItemStack dec(RemoteItemStack stack) {
        return stack.count() == 1 ? null : item(stack.legacyId(), stack.count() - 1); }
    private static RemoteItemStack[] none(int n) { return new RemoteItemStack[n]; }
    private static RemoteItemStack[] last(int n, RemoteItemStack result) {
        RemoteItemStack[] mid = new RemoteItemStack[n]; mid[n - 1] = result; return mid; }
}
