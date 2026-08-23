package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 take/place that seats or clears armor slots 5-8 through Packet102. */
public final class B173ArmorReductionEquip {
  private int action = 1;
  public B173ArmorReductionEquip() {
  }

  public void wear(B173WireClient actor, int[] items) {
    if (items == null || items.length != 4)
      throw new IllegalArgumentException("armor family must be four pieces");
    for (int index = 0; index < 4; index++) {
      int id = items[index], dest = 5 + index;
      if (id < 298 || id > 313)
        throw new IllegalArgumentException("armor id drifted");
      put(actor, id, dest);
    }
    RemoteInventoryView view = actor.inventory();
    for (int index = 0; index < 4; index++)
      if (view.slot(5 + index).empty() || view.slot(5 + index).item().legacyId() != items[index])
        throw new IllegalStateException("armor family window drifted");
  }

  public void strip(B173WireClient actor) {
    for (int dest = 5; dest <= 8; dest++)
      if (!actor.inventory().slot(dest).empty())
        take(actor, dest);
    RemoteInventoryView view = actor.inventory();
    if (!view.slot(5).empty() || !view.slot(6).empty() || !view.slot(7).empty()
        || !view.slot(8).empty())
      throw new IllegalStateException("armor slots were not cleared");
  }

  private void put(B173WireClient actor, int id, int dest) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      preflight(inbound);
      RemoteInventoryView before = inbound.inventory();
      int source = find(before, id);
      if (source < 9 || !before.slot(dest).empty())
        throw new IllegalStateException("armor source or destination drifted id=" + id);
      RemoteItemStack stack = before.slot(source).item();
      step(channel, inbound, source, stack, replace(before, source, null), stack);
      before = inbound.inventory();
      step(channel, inbound, dest, null, replace(before, dest, stack), null);
    } catch (IOException error) {
      throw new IllegalStateException("armor equip failed", error);
    }
  }

  private void take(B173WireClient actor, int dest) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      preflight(inbound);
      RemoteInventoryView before = inbound.inventory();
      if (before.slot(dest).empty())
        return;
      RemoteItemStack stack = before.slot(dest).item();
      int empty = emptySlot(before);
      step(channel, inbound, dest, stack, replace(before, dest, null), stack);
      before = inbound.inventory();
      empty = emptySlot(before);
      step(channel, inbound, empty, null, replace(before, empty, stack), null);
    } catch (IOException error) {
      throw new IllegalStateException("armor unequip failed", error);
    }
  }

  private void step(B173PlayChannel channel, B173PlayInbound inbound, int slot,
      RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter)
      throws IOException {
    if (action > 32767)
      throw new IllegalStateException("armor click counter exhausted");
    inbound.beginPersonalTransaction(
        action, slot, predicted, inbound.inventory(), after, inbound.cursor(), cursorAfter);
    B173ContainerPacket.write(channel.output, 0, slot, 0, action, predicted);
    channel.output.flush();
    inbound.awaitPersonalStep();
    action++;
  }

  private static void preflight(B173PlayInbound inbound) {
    if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
      throw new IllegalStateException("armor click preflight failed");
    RemoteInventoryView view = inbound.inventory();
    if (view.windowId() != 0 || view.size() != 45)
      throw new IllegalStateException("personal window drifted");
  }

  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 9; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }

  private static int emptySlot(RemoteInventoryView view) {
    for (int slot = 9; slot <= 44; slot++)
      if (view.slot(slot).empty())
        return slot;
    throw new IllegalStateException("no empty personal slot for unequip");
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(0, slots);
  }
}
