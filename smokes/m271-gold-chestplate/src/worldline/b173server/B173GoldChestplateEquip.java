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

/** Two accepted window-0 clicks that seat gold chestplate 315 in armor slot 6. */
public final class B173GoldChestplateEquip {
  static final int ITEM = 315, SOURCE = 36, ARMOR = 6;
  private B173GoldChestplateEquip() {
  }

  public static void verify() throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream output = new DataOutputStream(bytes);
    B173ContainerPacket.write(output, 0, SOURCE, 0, 1, new RemoteItemStack(ITEM, 1, 0));
    B173ContainerPacket.write(output, 0, ARMOR, 0, 2, null);
    DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    packet(input, SOURCE, 1, ITEM);
    packet(input, ARMOR, 2, -1);
    if (input.available() != 0 || bytes.size() != 23)
      throw new AssertionError("gold chestplate Packet102 sequence size drifted");
  }

  public static void apply(B173WireClient client) {
    try {
      B173PlayChannel channel = client.channel();
      B173PlayInbound inbound = channel.inbound();
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
        throw new IllegalStateException("gold chestplate equip preflight failed");
      RemoteItemStack stack = new RemoteItemStack(ITEM, 1, 0);
      RemoteInventoryView before = inbound.inventory();
      if (before.windowId() != 0 || before.size() != 45 || before.slot(SOURCE).empty()
          || !before.slot(SOURCE).item().equals(stack) || !before.slot(ARMOR).empty()
          || stack.legacyId() == 299 || stack.legacyId() == 307)
        throw new IllegalStateException("gold chestplate source or destination drifted");
      RemoteInventoryView taken = replace(before, SOURCE, null);
      step(channel, inbound, SOURCE, stack, taken, stack, 1);
      RemoteInventoryView after = replace(taken, ARMOR, stack);
      step(channel, inbound, ARMOR, null, after, null, 2);
    } catch (IOException error) {
      throw new IllegalStateException("gold chestplate equip failed", error);
    }
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

  private static void packet(DataInputStream input, int slot, int action, int item)
      throws Exception {
    if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 0
        || input.readShort() != slot || input.readUnsignedByte() != 0 || input.readShort() != action
        || input.readBoolean() || input.readShort() != item)
      throw new AssertionError("gold chestplate Packet102 fields drifted");
    if (item >= 0 && (input.readUnsignedByte() != 1 || input.readShort() != 0))
      throw new AssertionError("gold chestplate Packet102 stack drifted");
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(0, slots);
  }
}
