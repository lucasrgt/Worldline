package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 2x2: two sugar canes 338 become two sugar 353, cloned from M297 planks. */
public final class B173FoodSugarClick {
  public static final RemoteItemStack CANE2 = new RemoteItemStack(338, 2, 0);
  public static final RemoteItemStack CANE1 = new RemoteItemStack(338, 1, 0);
  public static final RemoteItemStack SUGAR = new RemoteItemStack(353, 1, 0);
  private B173FoodSugarClick() {
  }

  public static void apply(B173WireClient actor) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      RemoteInventoryView before = inbound.inventory();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
          || before.windowId() != 0 || before.size() != 45 || !emptyCraft(before)
          || !same(before, 40, CANE2) || SUGAR.legacyId() != 353)
        throw new IllegalStateException("sugar 2x2 preflight failed");
      step(channel, inbound, 1, 40, 0, CANE2, replace(before, 40, null), CANE2);
      step(channel, inbound, 2, 1, 1, null,
          replace(replace(inbound.inventory(), 1, CANE1), 0, SUGAR), CANE1);
      if (!same(inbound.inventory(), 0, SUGAR))
        throw new IllegalStateException("sugar result 353 absent from 2x2 cane");
      step(channel, inbound, 3, 40, 0, null, replace(inbound.inventory(), 40, CANE1), null);
      step(channel, inbound, 4, 0, 0, SUGAR,
          replace(replace(inbound.inventory(), 0, null), 1, null), SUGAR);
      step(channel, inbound, 5, 12, 0, null, replace(inbound.inventory(), 12, SUGAR), null);
      step(channel, inbound, 6, 40, 0, CANE1, replace(inbound.inventory(), 40, null), CANE1);
      step(channel, inbound, 7, 1, 0, null,
          replace(replace(inbound.inventory(), 1, CANE1), 0, SUGAR), null);
      step(channel, inbound, 8, 0, 0, SUGAR,
          replace(replace(inbound.inventory(), 0, null), 1, null), SUGAR);
      step(channel, inbound, 9, 13, 0, null, replace(inbound.inventory(), 13, SUGAR), null);
      if (!same(inbound.inventory(), 12, SUGAR) || !same(inbound.inventory(), 13, SUGAR)
          || !emptyCraft(inbound.inventory()) || inbound.cursor() != null)
        throw new IllegalStateException("sugar 353 2x2 store drifted");
    } catch (IOException error) {
      throw new IllegalStateException("sugar 2x2 click failed", error);
    }
  }

  private static void step(B173PlayChannel channel, B173PlayInbound inbound, int action, int slot,
      int button, RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter)
      throws IOException {
    inbound.beginPersonalTransaction(
        action, slot, predicted, inbound.inventory(), after, inbound.cursor(), cursorAfter);
    B173ContainerPacket.write(channel.output, 0, slot, button, action, predicted);
    channel.output.flush();
    inbound.awaitPersonalStep();
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(0, slots);
  }

  private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
    return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
  }

  public static boolean emptyCraft(RemoteInventoryView view) {
    for (int slot = 0; slot < 5; slot++)
      if (!view.slot(slot).empty())
        return false;
    return true;
  }
}
