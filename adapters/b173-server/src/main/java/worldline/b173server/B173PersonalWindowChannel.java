package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalCraft;
import worldline.api.RemotePersonalTransaction;
import worldline.api.RemoteArmorEquip;
import worldline.api.RemoteArmorSlot;

/** Exact left-click predictor for bounded personal-window take/place/swap transitions. */
final class B173PersonalWindowChannel {
    private final DataOutputStream output;
    private final B173PlayInbound inbound;
    private int action;

    B173PersonalWindowChannel(DataOutputStream output, B173PlayInbound inbound) {
        this.output = output; this.inbound = inbound; }

    RemotePersonalTransaction click(int slot) throws IOException { return click(slot, false); }
    RemotePersonalTransaction rejectedTakeProbe(int slot) throws IOException { return click(slot, true); }

    RemoteArmorEquip equipLeather(int personalSlot, RemoteArmorSlot armor) throws IOException {
        if (inbound.windowActive() || action > 32765 || armor == null || personalSlot < 9 || personalSlot > 44
                || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("leather armor equip preflight failed");
        RemoteInventoryView before = inbound.inventory(); RemoteItemStack stack =
                new RemoteItemStack(armor.leatherItemId(), 1, 0);
        if (before.windowId() != 0 || before.size() != 45 || before.slot(personalSlot).empty()
                || !before.slot(personalSlot).item().equals(stack) || !before.slot(armor.containerSlot()).empty())
            throw new IllegalStateException("leather armor source or destination drifted");
        RemoteInventoryView taken = replace(before, personalSlot, null);
        B173PersonalStep take = step(personalSlot, stack, taken, stack);
        RemoteInventoryView after = replace(taken, armor.containerSlot(), stack);
        B173PersonalStep place = step(armor.containerSlot(), null, after, null);
        return new RemoteArmorEquip(personalSlot, armor, take.action, place.action,
                stack, before, taken, after);
    }

    RemotePersonalCraft craft2x2(int slot) throws IOException {
        RemoteInventoryView before = inbound.inventory(); RemoteItemStack log = new RemoteItemStack(17, 1, 0);
        if (inbound.windowActive() || action > 32763 || slot < 9 || slot > 44 || !inbound.cursorObserved() || inbound.cursor() != null
                || before.slot(slot).empty() || !before.slot(slot).item().equals(log) || !emptyCraft(before))
            throw new IllegalStateException("personal 2x2 craft requires one log and an empty matrix/cursor");
        RemotePersonalTransaction take = click(slot, false); RemoteItemStack planks = new RemoteItemStack(5, 4, 0);
        RemoteInventoryView matrix = view(take.after(), 0, planks, 1, log);
        B173PersonalStep place = step(1, null, matrix, null);
        RemoteInventoryView crafted = view(place.after, 0, null, 1, null);
        B173PersonalStep result = step(0, planks, crafted, planks);
        RemotePersonalTransaction stored = click(slot, false);
        return new RemotePersonalCraft(slot, take.actionId(), place.action, result.action, stored.actionId(),
                log, planks, before, matrix, crafted, stored.after());
    }

    int personalProofSlot() {
        if (action == 32767 || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("personal-window proof requires an observed empty cursor");
        RemoteInventoryView view = inbound.inventory(); int slot = 9;
        while (slot <= 44 && !view.slot(slot).empty()) slot++;
        if (slot > 44) throw new IllegalStateException("personal-window proof requires an empty storage slot");
        return slot;
    }

    B173PersonalStep provePersonalWindow(int slot) throws IOException {
        RemoteInventoryView view = inbound.inventory();
        if (action == 32767 || !inbound.cursorObserved() || inbound.cursor() != null
                || slot < 9 || slot > 44 || !view.slot(slot).empty())
            throw new IllegalStateException("personal-window proof preflight drifted");
        return step(slot, null, view, null);
    }

    private RemotePersonalTransaction click(int slot, boolean staleEmptyPrediction) throws IOException {
        if (inbound.windowActive()) throw new IllegalStateException("personal window is not active");
        RemoteInventoryView before = inbound.inventory();
        if (before.windowId() != 0 || before.size() != 45 || slot < 9 || slot > 44)
            throw new IllegalArgumentException("invalid personal inventory slot");
        if (!inbound.cursorObserved()) throw new IllegalStateException("personal cursor is not observed");
        RemoteItemStack cursor = inbound.cursor();
        RemoteItemStack source = before.slot(slot).empty() ? null : before.slot(slot).item();
        if (source == null && cursor == null)
            throw new IllegalStateException("personal left click requires an occupied slot or cursor");
        RemoteItemStack predicted = source, nextCursor = source, nextSlot = cursor;
        if (staleEmptyPrediction && (source == null || cursor != null))
            throw new IllegalStateException("rejected-take probe requires occupied source and empty cursor");
        RemoteItemStack wirePrediction = staleEmptyPrediction ? null : predicted;
        List<RemoteInventorySlot> slots = new ArrayList<>(before.slots());
        slots.set(slot, new RemoteInventorySlot(slot, nextSlot));
        RemoteInventoryView after = new RemoteInventoryView(0, slots);
        if (action == 32767) throw new IllegalStateException("personal transaction counter exhausted");
        int nextAction = action + 1;
        inbound.beginPersonalTransaction(nextAction, slot, wirePrediction, before, after, cursor, nextCursor);
        B173ContainerPacket.write(output, 0, slot, 0, nextAction, wirePrediction);
        output.flush(); action = nextAction; return inbound.awaitPersonalTransaction();
    }

    private B173PersonalStep step(int slot, RemoteItemStack predicted,
            RemoteInventoryView after, RemoteItemStack cursorAfter) throws IOException {
        RemoteInventoryView before = inbound.inventory(); RemoteItemStack cursorBefore = inbound.cursor();
        if (action == 32767) throw new IllegalStateException("personal transaction counter exhausted");
        int nextAction = action + 1;
        inbound.beginPersonalTransaction(nextAction, slot, predicted, before, after, cursorBefore, cursorAfter);
        B173ContainerPacket.write(output, 0, slot, 0, nextAction, predicted);
        output.flush(); action = nextAction; return inbound.awaitPersonalStep();
    }

    private static RemoteInventoryView view(RemoteInventoryView source, int firstSlot,
            RemoteItemStack first, int secondSlot, RemoteItemStack second) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots());
        slots.set(firstSlot, new RemoteInventorySlot(firstSlot, first));
        slots.set(secondSlot, new RemoteInventorySlot(secondSlot, second));
        return new RemoteInventoryView(0, slots);
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item)); return new RemoteInventoryView(0, slots); }

    private static boolean emptyCraft(RemoteInventoryView view) {
        for (int slot = 0; slot < 5; slot++) if (!view.slot(slot).empty()) return false; return true; }
}
