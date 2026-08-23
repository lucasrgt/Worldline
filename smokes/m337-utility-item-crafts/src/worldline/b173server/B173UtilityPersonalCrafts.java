package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 crafts: diagonal iron to shears 359, iron+flint 318 to flint-and-steel 259. */
public final class B173UtilityPersonalCrafts {
  public static final RemoteItemStack SHEARS = new RemoteItemStack(359, 1, 0);
  public static final RemoteItemStack FLINT_STEEL = new RemoteItemStack(259, 1, 0);
  public static final RemoteItemStack IRON6 = new RemoteItemStack(265, 6, 0);
  public static final RemoteItemStack FLINT = new RemoteItemStack(318, 1, 0);
  private static final int IRON = 265;
  private B173UtilityPersonalCrafts() {
  }

  public static void apply(B173WireClient actor) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      RemoteInventoryView before = inbound.inventory();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
          || before.windowId() != 0 || before.size() != 45 || !emptyCraft(before)
          || !same(before, 38, IRON6) || !same(before, 39, FLINT) || !before.slot(37).empty()
          || SHEARS.legacyId() != 359 || FLINT_STEEL.legacyId() != 259)
        throw new IllegalStateException("utility 2x2 preflight failed");
      flintSteel(channel, inbound, shears(channel, inbound, 1));
      if (!stored(inbound.inventory()) || inbound.cursor() != null)
        throw new IllegalStateException("utility 2x2 results drifted");
    } catch (IOException error) {
      throw new IllegalStateException("utility 2x2 crafts failed", error);
    }
  }

  public static boolean stored(RemoteInventoryView view) {
    return same(view, 37, SHEARS) && same(view, 39, FLINT_STEEL) && emptyCraft(view);
  }

  private static int shears(B173PlayChannel channel, B173PlayInbound inbound, int action)
      throws IOException {
    action =
        step(channel, inbound, 38, 0, IRON6, replace(inbound.inventory(), 38, null), IRON6, action);
    action = step(
        channel, inbound, 2, 1, null, replace(inbound.inventory(), 2, item(1)), item(5), action);
    action = step(channel, inbound, 3, 1, null,
        replace(replace(inbound.inventory(), 3, item(1)), 0, SHEARS), item(4), action);
    if (!same(inbound.inventory(), 0, SHEARS))
      throw new IllegalStateException("shears 359 absent from diagonal 2x2");
    action = step(
        channel, inbound, 38, 0, null, replace(inbound.inventory(), 38, item(4)), null, action);
    action = step(channel, inbound, 0, 0, SHEARS,
        replace(replace(replace(inbound.inventory(), 0, null), 2, null), 3, null), SHEARS, action);
    return step(
        channel, inbound, 37, 0, null, replace(inbound.inventory(), 37, SHEARS), null, action);
  }

  private static int flintSteel(B173PlayChannel channel, B173PlayInbound inbound, int action)
      throws IOException {
    RemoteItemStack iron = item(4);
    action =
        step(channel, inbound, 38, 0, iron, replace(inbound.inventory(), 38, null), iron, action);
    action = step(
        channel, inbound, 1, 1, null, replace(inbound.inventory(), 1, item(1)), item(3), action);
    action = step(
        channel, inbound, 38, 0, null, replace(inbound.inventory(), 38, item(3)), null, action);
    action =
        step(channel, inbound, 39, 0, FLINT, replace(inbound.inventory(), 39, null), FLINT, action);
    action = step(channel, inbound, 4, 0, null,
        replace(replace(inbound.inventory(), 4, FLINT), 0, FLINT_STEEL), null, action);
    if (!same(inbound.inventory(), 0, FLINT_STEEL))
      throw new IllegalStateException("flint-and-steel 259 absent from 2x2");
    action = step(channel, inbound, 0, 0, FLINT_STEEL,
        replace(replace(replace(inbound.inventory(), 0, null), 1, null), 4, null), FLINT_STEEL,
        action);
    return step(
        channel, inbound, 39, 0, null, replace(inbound.inventory(), 39, FLINT_STEEL), null, action);
  }

  private static int step(B173PlayChannel channel, B173PlayInbound inbound, int slot, int button,
      RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter, int action)
      throws IOException {
    inbound.beginPersonalTransaction(
        action, slot, predicted, inbound.inventory(), after, inbound.cursor(), cursorAfter);
    B173ContainerPacket.write(channel.output, 0, slot, button, action, predicted);
    channel.output.flush();
    inbound.awaitPersonalStep();
    return action + 1;
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(0, slots);
  }

  private static RemoteItemStack item(int count) {
    return new RemoteItemStack(IRON, count, 0);
  }
  private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
    return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
  }
  private static boolean emptyCraft(RemoteInventoryView view) {
    for (int slot = 0; slot < 5; slot++)
      if (!view.slot(slot).empty())
        return false;
    return true;
  }
}
