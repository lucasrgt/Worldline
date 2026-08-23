package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Packet102 Trap load of oak planks 5, which RemoteDispenserLoad does not accept. */
public final class B173DispenserSetLoads {
  public static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0);
  public static final RemoteItemStack PLANKS = new RemoteItemStack(5, 1, 0);
  private final B173PlayChannel channel;
  private final B173PlayInbound inbound;
  private int windowId = -1, action;
  private long epoch = -1L;

  private B173DispenserSetLoads(B173WireClient actor) {
    channel = actor.channel();
    inbound = channel.inbound();
  }

  public static void planks(B173WireClient actor) {
    try {
      new B173DispenserSetLoads(actor).move(40, 1, PLANKS);
    } catch (IOException error) {
      throw new IllegalStateException("dispenser-set planks load failed", error);
    }
  }

  private void move(int personalSlot, int ownedSlot, RemoteItemStack expected) throws IOException {
    RemoteContainerWindow active = inbound.activeWindow();
    RemoteInventoryView before = active.inventory();
    int owned = active.descriptor().playerTailOffset(), combined = owned + personalSlot - 9;
    if (active.descriptor().kind() != RemoteWindowKind.DISPENSER || before.size() != 45
        || personalSlot < 9 || personalSlot > 44 || ownedSlot < 0 || ownedSlot >= owned
        || !inbound.cursorObserved() || inbound.cursor() != null || before.slot(combined).empty()
        || !before.slot(ownedSlot).empty() || !before.slot(combined).item().equals(expected))
      throw new IllegalStateException(
          "dispenser-set move requires occupied source and empty target");
    prepare(before, 2);
    RemoteItemStack stack = before.slot(combined).item();
    RemoteInventoryView personal = inbound.inventory();
    RemoteInventoryView taken = replace(before, combined, null),
                        personalTaken = replace(personal, personalSlot, null);
    step(combined, stack, taken, personal, personalTaken, stack);
    step(ownedSlot, null, replace(taken, ownedSlot, stack), personalTaken, personalTaken, null);
    RemoteInventoryView window = inbound.activeWindow().inventory();
    if (window.slot(ownedSlot).empty() || !window.slot(ownedSlot).item().equals(expected)
        || !inbound.inventory().slot(personalSlot).empty())
      throw new IllegalStateException("dispenser-set loaded stack drifted");
  }

  private void prepare(RemoteInventoryView view, int actions) {
    long activeEpoch = inbound.activeWindowEpoch();
    if (epoch != activeEpoch) {
      epoch = activeEpoch;
      windowId = view.windowId();
      action = 0;
    }
    if (action > 32767 - actions)
      throw new IllegalStateException("dispenser-set transaction counter exhausted");
  }

  private void step(int slot, RemoteItemStack predicted, RemoteInventoryView after,
      RemoteInventoryView personalBefore, RemoteInventoryView personalAfter,
      RemoteItemStack cursorAfter) throws IOException {
    RemoteInventoryView before = inbound.activeWindow().inventory();
    int nextAction = action + 1;
    inbound.beginContainerTransaction(new B173ContainerStep(windowId, nextAction, slot, predicted,
        before, after, personalBefore, personalAfter, inbound.cursor(), cursorAfter));
    B173ContainerPacket.write(channel.output, windowId, slot, 0, nextAction, predicted);
    channel.output.flush();
    action = nextAction;
    inbound.awaitContainerTransaction();
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(source.windowId(), slots);
  }
}
