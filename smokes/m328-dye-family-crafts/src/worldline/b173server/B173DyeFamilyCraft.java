package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Eighteen accepted window-0 clicks that mill dye powders in the personal 2x2 grid. */
public final class B173DyeFamilyCraft {
  private static final int[] SOURCE = {36, 37, 38}, INPUT_IDS = {352, 38, 37};
  private static final int[] RESULT_COUNTS = {3, 2, 2}, RESULT_DAMAGES = {15, 1, 11};
  private static final int[] CLICK_SLOTS = {
      36, 1, 0, 36, 37, 1, 0, 37, 38, 1, 0, 38, 39, 1, 40, 2, 0, 39};
  private static final int[] CLICK_ITEMS = {
      352, -1, 351, -1, 38, -1, 351, -1, 37, -1, 351, -1, 351, -1, 351, -1, 351, -1};
  private static final int[] CLICK_COUNTS = {1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 2, 0, 1, 0, 1, 0, 2, 0};
  private static final int[] CLICK_DAMAGE = {
      0, 0, 15, 0, 0, 0, 1, 0, 0, 0, 11, 0, 0, 0, 15, 0, 8, 0};

  private B173DyeFamilyCraft() {
  }

  public static void verify() throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream output = new DataOutputStream(bytes);
    for (int index = 0; index < CLICK_SLOTS.length; index++) {
      int item = CLICK_ITEMS[index];
      B173ContainerPacket.write(output, 0, CLICK_SLOTS[index], 0, index + 1,
          item < 0 ? null : new RemoteItemStack(item, CLICK_COUNTS[index], CLICK_DAMAGE[index]));
    }
    DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    for (int index = 0; index < CLICK_SLOTS.length; index++)
      packet(input, CLICK_SLOTS[index], index + 1, CLICK_ITEMS[index], CLICK_COUNTS[index],
          CLICK_DAMAGE[index]);
    if (input.available() != 0 || bytes.size() != 207)
      throw new AssertionError("dye-family Packet102 sequence size drifted");
  }

  public static void apply(B173WireClient actor) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
        throw new IllegalStateException("dye-family craft preflight failed");
      int action = 1;
      for (int index = 0; index < SOURCE.length; index++)
        action = mill(channel, inbound, SOURCE[index], new RemoteItemStack(INPUT_IDS[index], 1, 0),
            new RemoteItemStack(351, RESULT_COUNTS[index], RESULT_DAMAGES[index]), action);
      action = mix(channel, inbound, action);
      if (action != 19 || inbound.cursor() != null)
        throw new IllegalStateException("dye-family action drift");
      RemoteInventoryView view = inbound.inventory();
      if (view.occupiedSlots() != 4 || !same(view, 36, 351, 3, 15) || !same(view, 37, 351, 2, 1)
          || !same(view, 38, 351, 2, 11) || !same(view, 39, 351, 2, 8) || !emptyCraft(view)
          || !view.slot(40).empty())
        throw new IllegalStateException("dye-family results drifted");
    } catch (IOException error) {
      throw new IllegalStateException("dye-family craft failed", error);
    }
  }

  private static int mill(B173PlayChannel channel, B173PlayInbound inbound, int slot,
      RemoteItemStack input, RemoteItemStack result, int action) throws IOException {
    RemoteInventoryView before = inbound.inventory();
    if (slot < 9 || slot > 44 || inbound.cursor() != null || !same(before, slot, input)
        || !emptyCraft(before) || result.legacyId() != 351)
      throw new IllegalStateException("dye-family ingredients drifted");
    step(channel, inbound, slot, input, replace(before, slot, null), input, action++);
    RemoteInventoryView matrix = replace(replace(inbound.inventory(), 1, input), 0, result);
    step(channel, inbound, 1, null, matrix, null, action++);
    RemoteInventoryView crafted = replace(replace(inbound.inventory(), 0, null), 1, null);
    step(channel, inbound, 0, result, crafted, result, action++);
    step(channel, inbound, slot, null, replace(inbound.inventory(), slot, result), null, action++);
    if (!same(inbound.inventory(), slot, result) || !emptyCraft(inbound.inventory())
        || inbound.cursor() != null)
      throw new IllegalStateException("dye-family store drifted");
    return action;
  }

  private static int mix(B173PlayChannel channel, B173PlayInbound inbound, int action)
      throws IOException {
    RemoteItemStack ink = new RemoteItemStack(351, 1, 0);
    RemoteItemStack meal = new RemoteItemStack(351, 1, 15);
    RemoteItemStack gray = new RemoteItemStack(351, 2, 8);
    RemoteInventoryView before = inbound.inventory();
    if (inbound.cursor() != null || !same(before, 39, ink) || !same(before, 40, meal)
        || !emptyCraft(before) || !same(before, 36, 351, 3, 15) || !same(before, 37, 351, 2, 1)
        || !same(before, 38, 351, 2, 11))
      throw new IllegalStateException("dye-family mix ingredients drifted");
    step(channel, inbound, 39, ink, replace(before, 39, null), ink, action++);
    step(channel, inbound, 1, null, replace(inbound.inventory(), 1, ink), null, action++);
    step(channel, inbound, 40, meal, replace(inbound.inventory(), 40, null), meal, action++);
    RemoteInventoryView matrix = replace(replace(inbound.inventory(), 2, meal), 0, gray);
    step(channel, inbound, 2, null, matrix, null, action++);
    RemoteInventoryView crafted =
        replace(replace(replace(inbound.inventory(), 0, null), 1, null), 2, null);
    step(channel, inbound, 0, gray, crafted, gray, action++);
    step(channel, inbound, 39, null, replace(inbound.inventory(), 39, gray), null, action++);
    if (!same(inbound.inventory(), 39, gray) || !emptyCraft(inbound.inventory())
        || inbound.cursor() != null || !inbound.inventory().slot(40).empty())
      throw new IllegalStateException("dye-family mix store drifted");
    return action;
  }

  private static void step(B173PlayChannel channel, B173PlayInbound inbound, int slot,
      RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter, int action)
      throws IOException {
    inbound.beginPersonalTransaction(
        action, slot, predicted, inbound.inventory(), after, inbound.cursor(), cursorAfter);
    B173ContainerPacket.write(channel.output, 0, slot, 0, action, predicted);
    channel.output.flush();
    inbound.awaitPersonalStep();
  }

  private static void packet(DataInputStream input, int slot, int action, int item, int count,
      int damage) throws Exception {
    if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 0
        || input.readShort() != slot || input.readUnsignedByte() != 0 || input.readShort() != action
        || input.readBoolean() || input.readShort() != item)
      throw new AssertionError("dye-family Packet102 fields drifted");
    if (item >= 0 && (input.readUnsignedByte() != count || input.readShort() != damage))
      throw new AssertionError("dye-family Packet102 stack drifted");
  }

  private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
    return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
  }

  private static boolean same(RemoteInventoryView view, int slot, int id, int count, int damage) {
    return same(view, slot, new RemoteItemStack(id, count, damage));
  }

  private static boolean emptyCraft(RemoteInventoryView view) {
    for (int slot = 0; slot < 5; slot++)
      if (!view.slot(slot).empty())
        return false;
    return true;
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(0, slots);
  }
}
