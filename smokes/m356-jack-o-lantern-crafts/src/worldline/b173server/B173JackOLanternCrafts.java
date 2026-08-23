package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 crafts jack-o-lantern 91 from pumpkin 86 over torch 50. */
public final class B173JackOLanternCrafts {
  public static final RemoteItemStack PUMPKINS = new RemoteItemStack(86, 2, 0);
  public static final RemoteItemStack PUMPKIN = new RemoteItemStack(86, 1, 0);
  public static final RemoteItemStack TORCH = new RemoteItemStack(50, 1, 0);
  public static final RemoteItemStack LANTERN = new RemoteItemStack(91, 1, 0);
  private B173JackOLanternCrafts() {
  }

  public static void apply(B173WireClient actor) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      RemoteInventoryView before = inbound.inventory();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
          || before.windowId() != 0 || before.size() != 45 || !emptyCraft(before)
          || !same(before, 37, PUMPKINS) || !same(before, 38, TORCH) || PUMPKIN.legacyId() != 86
          || TORCH.legacyId() != 50 || LANTERN.legacyId() != 91)
        throw new IllegalStateException("jack-o-lantern craft preflight failed");
      step(channel, inbound, 1, 37, 0, PUMPKINS, replace(before, 37, null), PUMPKINS);
      step(channel, inbound, 2, 1, 1, null, replace(inbound.inventory(), 1, PUMPKIN), PUMPKIN);
      step(channel, inbound, 3, 37, 0, null, replace(inbound.inventory(), 37, PUMPKIN), null);
      step(channel, inbound, 4, 38, 0, TORCH, replace(inbound.inventory(), 38, null), TORCH);
      step(channel, inbound, 5, 3, 0, null,
          replace(replace(inbound.inventory(), 3, TORCH), 0, LANTERN), null);
      if (!same(inbound.inventory(), 0, LANTERN))
        throw new IllegalStateException("jack-o-lantern 91 absent from pumpkin-over-torch");
      step(channel, inbound, 6, 0, 0, LANTERN,
          replace(replace(replace(inbound.inventory(), 0, null), 1, null), 3, null), LANTERN);
      step(channel, inbound, 7, 38, 0, null, replace(inbound.inventory(), 38, LANTERN), null);
      if (!stored(inbound.inventory()) || inbound.cursor() != null)
        throw new IllegalStateException("jack-o-lantern craft store drifted");
    } catch (IOException error) {
      throw new IllegalStateException("jack-o-lantern craft failed", error);
    }
  }

  public static boolean stored(RemoteInventoryView view) {
    return same(view, 37, PUMPKIN) && same(view, 38, LANTERN) && emptyCraft(view);
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
