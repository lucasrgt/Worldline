package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;
import worldline.api.RemoteWorkbenchOutput;
import worldline.api.RemoteWorkbenchPreparation;

/** Exact byte-owned workbench row preparation and output completion. */
final class B173WorkbenchChannel {
    private final DataOutputStream output; private final B173PlayInbound inbound;
    private int windowId = -1, action; private long epoch = -1L;
    B173WorkbenchChannel(DataOutputStream output, B173PlayInbound inbound) {
        this.output = output; this.inbound = inbound; }

    RemoteWorkbenchPreparation prepareSlabs(int personalSlot) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        RemoteInventoryView personalBefore = inbound.inventory(); int source = personalSlot + 1;
        RemoteItemStack planks = item(5, 3, 0), one = item(5, 1, 0);
        if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || before.size() != 46
                || personalSlot < 9 || personalSlot > 44 || before.slot(source).empty()
                || !before.slot(source).item().equals(planks) || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid workbench preparation boundary");
        for (int slot = 0; slot < 10; slot++) if (!before.slot(slot).empty())
            throw new IllegalStateException("workbench preparation requires an empty result and matrix");
        prepare(before, 4); RemoteInventoryView personalAfter = replace(personalBefore, personalSlot, null);
        B173ContainerStep take = step(source, 0, planks, replace(before, source, null),
                personalBefore, personalAfter, planks); B173ContainerStep first = step(1, 1, null,
                replace(take.after, 1, one), personalAfter, personalAfter, item(5, 2, 0));
        RemoteInventoryView twoWide = replace(replace(first.after, 2, one), 0, item(72, 1, 0));
        B173ContainerStep second = step(2, 1, null, twoWide, personalAfter, personalAfter, one);
        RemoteInventoryView prepared = replace(replace(second.after, 3, one), 0, item(44, 3, 2));
        B173ContainerStep third = step(3, 1, null, prepared, personalAfter, personalAfter, null);
        return new RemoteWorkbenchPreparation(personalSlot, take.action, first.action, second.action,
                third.action, planks, item(72, 1, 0), item(44, 3, 2), take.cursorAfter,
                first.cursorAfter, second.cursorAfter, third.cursorAfter == null, before, first.after,
                twoWide, third.after, personalBefore, personalAfter);
    }

    RemoteWorkbenchOutput takeSlabs(int personalSlot) throws IOException {
        RemoteContainerWindow active = inbound.activeWindow(); RemoteInventoryView before = active.inventory();
        RemoteInventoryView personalBefore = inbound.inventory(); int combined = personalSlot + 1;
        RemoteItemStack slabs = item(44, 3, 2), plank = item(5, 1, 0);
        if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || before.size() != 46
                || personalSlot < 9 || personalSlot > 44 || !same(before, 0, slabs)
                || !same(before, 1, plank) || !same(before, 2, plank) || !same(before, 3, plank)
                || !before.slot(combined).empty() || !personalBefore.slot(personalSlot).empty()
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("invalid workbench output boundary");
        for (int slot = 4; slot < 10; slot++) if (!before.slot(slot).empty())
            throw new IllegalStateException("unexpected workbench matrix item");
        prepare(before, 2); RemoteInventoryView taken = before;
        for (int slot = 0; slot <= 3; slot++) taken = replace(taken, slot, null);
        B173ContainerStep take = step(0, 0, slabs, taken, personalBefore, personalBefore, slabs,
                16842796, 3); RemoteInventoryView stored = replace(take.after, combined, slabs);
        RemoteInventoryView personalAfter = replace(personalBefore, personalSlot, slabs);
        B173ContainerStep store = step(combined, 0, null, stored, personalBefore, personalAfter, null);
        return new RemoteWorkbenchOutput(personalSlot, take.action, store.action, 3, slabs,
                before, take.after, store.after, personalBefore, personalAfter);
    }

    private void prepare(RemoteInventoryView view, int actions) {
        long current = inbound.activeWindowEpoch(); if (epoch != current) {
            epoch = current; windowId = view.windowId(); action = 0; }
        if (action > 32767 - actions) throw new IllegalStateException("workbench action counter exhausted");
    }
    private B173ContainerStep step(int slot, int button, RemoteItemStack predicted,
            RemoteInventoryView after, RemoteInventoryView personalBefore,
            RemoteInventoryView personalAfter, RemoteItemStack cursorAfter) throws IOException {
        B173ContainerStep value = new B173ContainerStep(windowId, action + 1, slot, button, predicted,
                inbound.activeWindow().inventory(), after, personalBefore, personalAfter, inbound.cursor(), cursorAfter);
        send(value); action++; return inbound.awaitContainerTransaction();
    }
    private B173ContainerStep step(int slot, int button, RemoteItemStack predicted,
            RemoteInventoryView after, RemoteInventoryView personalBefore, RemoteInventoryView personalAfter,
            RemoteItemStack cursorAfter, int statisticId, int increment) throws IOException {
        B173ContainerStep value = new B173ContainerStep(windowId, action + 1, slot, predicted,
                inbound.activeWindow().inventory(), after, personalBefore, personalAfter,
                inbound.cursor(), cursorAfter, statisticId, increment); send(value); action++;
        return inbound.awaitContainerTransaction();
    }
    private void send(B173ContainerStep value) throws IOException { inbound.beginContainerTransaction(value);
        B173ContainerPacket.write(output, windowId, value.slot, value.button, value.action, value.predicted); output.flush(); }
    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots()); slots.set(slot,
                new RemoteInventorySlot(slot, item)); return new RemoteInventoryView(source.windowId(), slots); }
    private static RemoteItemStack item(int id, int count, int damage) { return new RemoteItemStack(id, count, damage); }
    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item); }
}
