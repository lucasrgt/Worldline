package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Accepted window-0 take/place that moves diamond leggings 312 into armor slot 7. */
public final class B173DiamondLeggingsClick {
  public static final RemoteItemStack LEGGINGS = new RemoteItemStack(312, 1, 0);
  private B173DiamondLeggingsClick() {
  }

  public static void apply(B173WireClient actor, int personalSlot) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
          || LEGGINGS.legacyId() == 300)
        throw new IllegalStateException("diamond leggings click preflight failed");
      RemoteInventoryView before = inbound.inventory();
      if (before.windowId() != 0 || before.size() != 45 || personalSlot < 9 || personalSlot > 44
          || before.slot(personalSlot).empty() || !before.slot(personalSlot).item().equals(LEGGINGS)
          || !before.slot(RemoteArmorSlot.LEGGINGS.containerSlot()).empty())
        throw new IllegalStateException("diamond leggings source or destination drifted");
      RemoteInventoryView taken = replace(before, personalSlot, null);
      step(channel, inbound, 1, personalSlot, LEGGINGS, taken, LEGGINGS);
      RemoteInventoryView after =
          replace(taken, RemoteArmorSlot.LEGGINGS.containerSlot(), LEGGINGS);
      step(channel, inbound, 2, RemoteArmorSlot.LEGGINGS.containerSlot(), null, after, null);
    } catch (IOException error) {
      throw new IllegalStateException("diamond leggings click failed", error);
    }
  }

  private static void step(B173PlayChannel channel, B173PlayInbound inbound, int action, int slot,
      RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter)
      throws IOException {
    inbound.beginPersonalTransaction(
        action, slot, predicted, inbound.inventory(), after, inbound.cursor(), cursorAfter);
    B173ContainerPacket.write(channel.output, 0, slot, 0, action, predicted);
    channel.output.flush();
    inbound.awaitPersonalStep();
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(0, slots);
  }
}
