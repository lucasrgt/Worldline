package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 crafts: log 17 to planks 5x4, vertical planks to sticks 280x4, coal 263 over stick 280 to torch 50x4. */
public final class B173BasicCraftsClick {
  public static final RemoteItemStack LOG = new RemoteItemStack(17, 1, 0);
  public static final RemoteItemStack PLANKS4 = new RemoteItemStack(5, 4, 0);
  public static final RemoteItemStack PLANKS3 = new RemoteItemStack(5, 3, 0);
  public static final RemoteItemStack PLANKS2 = new RemoteItemStack(5, 2, 0);
  public static final RemoteItemStack PLANK = new RemoteItemStack(5, 1, 0);
  public static final RemoteItemStack COAL = new RemoteItemStack(263, 1, 0);
  public static final RemoteItemStack STICKS = new RemoteItemStack(280, 4, 0);
  public static final RemoteItemStack STICKS3 = new RemoteItemStack(280, 3, 0);
  public static final RemoteItemStack STICK = new RemoteItemStack(280, 1, 0);
  public static final RemoteItemStack TORCHES = new RemoteItemStack(50, 4, 0);
  private B173BasicCraftsClick() {
  }

  public static void planks(B173WireClient actor) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      RemoteInventoryView before = inbound.inventory();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
          || before.windowId() != 0 || before.size() != 45 || !emptyCraft(before)
          || !same(before, 36, LOG) || !same(before, 37, COAL) || PLANKS4.legacyId() != 5)
        throw new IllegalStateException("plank craft preflight failed");
      step(channel, inbound, 1, 36, 0, LOG, with(before, 36, null), LOG);
      step(channel, inbound, 2, 1, 0, null, with(with(inbound.inventory(), 1, LOG), 0, PLANKS4),
          null);
      if (!same(inbound.inventory(), 0, PLANKS4))
        throw new IllegalStateException("plank result 5x4 absent from 2x2 log");
      step(channel, inbound, 3, 0, 0, PLANKS4, with(with(inbound.inventory(), 0, null), 1, null),
          PLANKS4);
      step(channel, inbound, 4, 36, 0, null, with(inbound.inventory(), 36, PLANKS4), null);
    } catch (IOException error) {
      throw new IllegalStateException("plank craft click failed", error);
    }
  }

  public static void sticks(B173WireClient actor) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      RemoteInventoryView before = inbound.inventory();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
          || !same(before, 36, PLANKS4) || !same(before, 37, COAL) || !emptyCraft(before)
          || STICKS.legacyId() != 280)
        throw new IllegalStateException("stick craft preflight failed");
      step(channel, inbound, 5, 36, 0, PLANKS4, with(before, 36, null), PLANKS4);
      step(channel, inbound, 6, 1, 1, null, with(inbound.inventory(), 1, PLANK), PLANKS3);
      step(channel, inbound, 7, 3, 1, null, with(with(inbound.inventory(), 3, PLANK), 0, STICKS),
          PLANKS2);
      if (!same(inbound.inventory(), 0, STICKS)
          || inbound.inventory().slot(0).item().legacyId() != 280)
        throw new IllegalStateException("stick result 280 absent from vertical 2x2");
      step(channel, inbound, 8, 36, 0, null, with(inbound.inventory(), 36, PLANKS2), null);
      step(channel, inbound, 9, 0, 0, STICKS,
          with(with(with(inbound.inventory(), 0, null), 1, null), 3, null), STICKS);
      step(channel, inbound, 10, 38, 0, null, with(inbound.inventory(), 38, STICKS), null);
    } catch (IOException error) {
      throw new IllegalStateException("stick craft click failed", error);
    }
  }

  public static void torches(B173WireClient actor) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      RemoteInventoryView before = inbound.inventory();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
          || !same(before, 36, PLANKS2) || !same(before, 37, COAL) || !same(before, 38, STICKS)
          || !emptyCraft(before) || TORCHES.legacyId() != 50)
        throw new IllegalStateException("torch craft preflight failed");
      step(channel, inbound, 11, 37, 0, COAL, with(before, 37, null), COAL);
      step(channel, inbound, 12, 1, 0, null, with(inbound.inventory(), 1, COAL), null);
      step(channel, inbound, 13, 38, 0, STICKS, with(inbound.inventory(), 38, null), STICKS);
      step(channel, inbound, 14, 3, 1, null, with(with(inbound.inventory(), 3, STICK), 0, TORCHES),
          STICKS3);
      if (!same(inbound.inventory(), 0, TORCHES)
          || inbound.inventory().slot(0).item().legacyId() != 50)
        throw new IllegalStateException("torch result 50 absent from coal-over-stick");
      step(channel, inbound, 15, 38, 0, null, with(inbound.inventory(), 38, STICKS3), null);
      step(channel, inbound, 16, 0, 0, TORCHES,
          with(with(with(inbound.inventory(), 0, null), 1, null), 3, null), TORCHES);
      step(channel, inbound, 17, 37, 0, null, with(inbound.inventory(), 37, TORCHES), null);
    } catch (IOException error) {
      throw new IllegalStateException("torch craft click failed", error);
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

  private static RemoteInventoryView with(
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
