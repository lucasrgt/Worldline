package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWindowKind;

/** Smoke-local Packet102 furnace load and Packet103 output wait for cactus, log, and clay. */
public final class B173RemainingFurnaceSmelts {
  private static final RemoteItemStack COAL = new RemoteItemStack(263, 1, 0);
  private static final RemoteItemStack GREEN = new RemoteItemStack(351, 1, 2);
  private static final RemoteItemStack CHARCOAL = new RemoteItemStack(263, 1, 1);
  private static final RemoteItemStack BRICK = new RemoteItemStack(336, 1, 0);
  private static final RemoteItemStack IRON = new RemoteItemStack(15, 1, 0);
  private static final RemoteItemStack INGOT = new RemoteItemStack(265, 1, 0);

  private B173RemainingFurnaceSmelts() {
  }

  public static RemoteItemStack smelt(B173WireClient actor, BlockPosition furnace, int inputSlot,
      int fuelSlot, RemoteItemStack input, RemoteItemStack output) throws Exception {
    requireRemaining(input, output);
    RemoteContainerWindow opened = actor.openFurnace(furnace, BlockFace.UP);
    int inputCombined = inputSlot - 6, fuelCombined = fuelSlot - 6;
    require(opened.descriptor().kind() == RemoteWindowKind.FURNACE
            && opened.descriptor().containerSlots() == 3
            && "Furnace".equals(opened.descriptor().title()) && opened.inventory().size() == 39
            && opened.inventory().slot(inputCombined).item().equals(input)
            && opened.inventory().slot(fuelCombined).item().equals(COAL)
            && opened.inventory().slot(0).empty() && opened.inventory().slot(1).empty()
            && opened.inventory().slot(2).empty() && !input.equals(IRON),
        "remaining furnace open mapping drifted");
    load(actor, inputSlot, fuelSlot, input);
    worldline.test.WorldlineSmokeAwait.observe(actor, 5);
    RemoteItemStack ready = awaitOutput(actor, output);
    RemoteWindowClosure closure = actor.closeWindow();
    require(closure.closedWindow().inventory().slot(2).item().equals(output)
            && closure.proofAction() >= 1,
        "remaining furnace close proof drifted");
    return ready;
  }

  private static void load(B173WireClient actor, int inputSlot, int fuelSlot, RemoteItemStack input)
      throws IOException {
    B173PlayChannel channel = actor.channel();
    B173PlayInbound inbound = channel.inbound();
    int action = 1;
    action = move(channel, inbound, inputSlot, 0, action);
    action = move(channel, inbound, fuelSlot, 1, action);
    require(action == 5 && inbound.inventory().slot(inputSlot).empty()
            && inbound.inventory().slot(fuelSlot).empty()
            && inbound.activeWindow().inventory().slot(0).item().equals(input)
            && inbound.cursor() == null,
        "remaining furnace load drifted");
  }

  private static int move(B173PlayChannel channel, B173PlayInbound inbound, int personalSlot,
      int ownedSlot, int action) throws IOException {
    RemoteContainerWindow active = inbound.activeWindow();
    RemoteInventoryView before = active.inventory();
    int combined = active.descriptor().playerTailOffset() + personalSlot - 9;
    RemoteItemStack stack = before.slot(combined).item();
    RemoteInventoryView personal = inbound.inventory();
    RemoteInventoryView taken = replace(before, combined, null);
    RemoteInventoryView personalTaken = replace(personal, personalSlot, null);
    step(channel, inbound, combined, stack, taken, personal, personalTaken, stack, action++);
    step(channel, inbound, ownedSlot, null,
        replace(inbound.activeWindow().inventory(), ownedSlot, stack), personalTaken, personalTaken,
        null, action++);
    return action;
  }

  private static void step(B173PlayChannel channel, B173PlayInbound inbound, int slot,
      RemoteItemStack predicted, RemoteInventoryView after, RemoteInventoryView personalBefore,
      RemoteInventoryView personalAfter, RemoteItemStack cursorAfter, int action)
      throws IOException {
    RemoteContainerWindow active = inbound.activeWindow();
    B173ContainerStep step = new B173ContainerStep(active.descriptor().windowId(), action, slot,
        predicted, active.inventory(), after, personalBefore, personalAfter, inbound.cursor(),
        cursorAfter);
    inbound.beginContainerTransaction(step);
    B173ContainerPacket.write(channel.output, step.windowId, slot, 0, action, predicted);
    channel.output.flush();
    inbound.awaitContainerTransaction();
  }

  private static RemoteItemStack awaitOutput(B173WireClient actor, RemoteItemStack output)
      throws IOException {
    B173PlayInbound inbound = actor.channel().inbound();
    Thread pulse = inbound.pulse();
    try {
      for (int count = 0; count < 8192; count++) {
        inbound.pumpOne();
        RemoteInventoryView view = inbound.activeWindow().inventory();
        if (!view.slot(2).empty() && view.slot(2).item().equals(output) && view.slot(0).empty()
            && view.slot(1).empty() && !output.equals(INGOT))
          return output;
      }
      throw new IOException("remaining furnace Packet103 output absent");
    } finally {
      pulse.interrupt();
    }
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(source.windowId(), slots);
  }

  private static void requireRemaining(RemoteItemStack input, RemoteItemStack output) {
    require((input.equals(new RemoteItemStack(81, 1, 0)) && output.equals(GREEN))
            || (input.equals(new RemoteItemStack(17, 1, 0)) && output.equals(CHARCOAL))
            || (input.equals(new RemoteItemStack(337, 1, 0)) && output.equals(BRICK)),
        "remaining furnace identity drifted");
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
