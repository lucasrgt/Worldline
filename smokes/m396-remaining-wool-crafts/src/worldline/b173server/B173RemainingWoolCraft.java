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

/** Eighteen accepted window-0 clicks that dye white wool 35:0 with magenta, light blue, and lime. */
public final class B173RemainingWoolCraft {
  private static final int[] WOOL_SLOTS = {36, 37, 38}, DYE_SLOTS = {39, 40, 41},
                             RESULTS = {2, 3, 5};
  private static final int[] CLICK_SLOTS = {
      36, 1, 39, 2, 0, 36, 37, 1, 40, 2, 0, 37, 38, 1, 41, 2, 0, 38};
  private static final int[] CLICK_ITEMS = {
      35, -1, 351, -1, 35, -1, 35, -1, 351, -1, 35, -1, 35, -1, 351, -1, 35, -1};
  private static final int[] CLICK_DAMAGE = {
      0, 0, 13, 0, 2, 0, 0, 0, 12, 0, 3, 0, 0, 0, 10, 0, 5, 0};

  private B173RemainingWoolCraft() {
  }

  public static void verify() throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream output = new DataOutputStream(bytes);
    for (int index = 0; index < CLICK_SLOTS.length; index++) {
      int item = CLICK_ITEMS[index];
      B173ContainerPacket.write(output, 0, CLICK_SLOTS[index], 0, index + 1,
          item < 0 ? null : new RemoteItemStack(item, 1, CLICK_DAMAGE[index]));
    }
    DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    for (int index = 0; index < CLICK_SLOTS.length; index++)
      packet(input, CLICK_SLOTS[index], index + 1, CLICK_ITEMS[index], CLICK_DAMAGE[index]);
    if (input.available() != 0 || bytes.size() != 207)
      throw new AssertionError("remaining dyed-wool Packet102 sequence size drifted");
  }

  public static void apply(B173WireClient actor) {
    try {
      B173PlayChannel channel = actor.channel();
      B173PlayInbound inbound = channel.inbound();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
        throw new IllegalStateException("remaining dyed-wool craft preflight failed");
      int action = 1;
      for (int index = 0; index < RESULTS.length; index++)
        action = dye(channel, inbound, WOOL_SLOTS[index], DYE_SLOTS[index], RESULTS[index], action);
      if (action != 19 || inbound.cursor() != null)
        throw new IllegalStateException("remaining dyed-wool action drift");
      RemoteInventoryView view = inbound.inventory();
      if (view.occupiedSlots() != 3 || !same(view, 36, 35, 2) || !same(view, 37, 35, 3)
          || !same(view, 38, 35, 5) || !emptyCraft(view))
        throw new IllegalStateException("remaining dyed-wool results drifted");
    } catch (IOException error) {
      throw new IllegalStateException("remaining dyed-wool craft failed", error);
    }
  }

  private static int dye(B173PlayChannel channel, B173PlayInbound inbound, int woolSlot,
      int dyeSlot, int resultDamage, int action) throws IOException {
    RemoteItemStack wool = new RemoteItemStack(35, 1, 0);
    RemoteItemStack dye = new RemoteItemStack(351, 1, 15 - resultDamage);
    RemoteItemStack result = new RemoteItemStack(35, 1, resultDamage);
    RemoteInventoryView before = inbound.inventory();
    if (woolSlot < 9 || dyeSlot < 9 || woolSlot > 44 || dyeSlot > 44 || inbound.cursor() != null
        || !same(before, woolSlot, wool) || !same(before, dyeSlot, dye) || !emptyCraft(before))
      throw new IllegalStateException("remaining dyed-wool ingredients drifted");
    step(channel, inbound, woolSlot, wool, replace(before, woolSlot, null), wool, action++);
    step(channel, inbound, 1, null, replace(inbound.inventory(), 1, wool), null, action++);
    step(
        channel, inbound, dyeSlot, dye, replace(inbound.inventory(), dyeSlot, null), dye, action++);
    RemoteInventoryView matrix = replace(replace(inbound.inventory(), 2, dye), 0, result);
    step(channel, inbound, 2, null, matrix, null, action++);
    RemoteInventoryView crafted =
        replace(replace(replace(inbound.inventory(), 0, null), 1, null), 2, null);
    step(channel, inbound, 0, result, crafted, result, action++);
    step(channel, inbound, woolSlot, null, replace(inbound.inventory(), woolSlot, result), null,
        action++);
    if (!same(inbound.inventory(), woolSlot, result) || !emptyCraft(inbound.inventory())
        || inbound.cursor() != null)
      throw new IllegalStateException("remaining dyed-wool store drifted");
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

  private static void packet(DataInputStream input, int slot, int action, int item, int damage)
      throws Exception {
    if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 0
        || input.readShort() != slot || input.readUnsignedByte() != 0 || input.readShort() != action
        || input.readBoolean() || input.readShort() != item)
      throw new AssertionError("remaining dyed-wool Packet102 fields drifted");
    if (item >= 0 && (input.readUnsignedByte() != 1 || input.readShort() != damage))
      throw new AssertionError("remaining dyed-wool Packet102 stack drifted");
  }

  private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
    return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
  }

  private static boolean same(RemoteInventoryView view, int slot, int id, int damage) {
    return same(view, slot, new RemoteItemStack(id, 1, damage));
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
