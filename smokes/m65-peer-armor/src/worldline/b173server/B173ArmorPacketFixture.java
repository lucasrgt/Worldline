package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteItemStack;

/** Byte oracle for the exact eight left-click Packet102 messages used by M65. */
public final class B173ArmorPacketFixture {
  private B173ArmorPacketFixture() {
  }
  public static void verify() throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream output = new DataOutputStream(bytes);
    int source = 36, action = 1;
    for (RemoteArmorSlot slot : RemoteArmorSlot.values()) {
      B173ContainerPacket.write(
          output, 0, source++, 0, action++, new RemoteItemStack(slot.leatherItemId(), 1, 0));
      B173ContainerPacket.write(output, 0, slot.containerSlot(), 0, action++, null);
    }
    DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    source = 36;
    action = 1;
    for (RemoteArmorSlot slot : RemoteArmorSlot.values()) {
      packet(input, source++, action++, slot.leatherItemId());
      packet(input, slot.containerSlot(), action++, -1);
    }
    if (input.available() != 0 || bytes.size() != 92)
      throw new AssertionError("armor Packet102 sequence size drifted");
  }
  private static void packet(DataInputStream input, int slot, int action, int item)
      throws Exception {
    if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 0
        || input.readShort() != slot || input.readUnsignedByte() != 0 || input.readShort() != action
        || input.readBoolean() || input.readShort() != item)
      throw new AssertionError("armor Packet102 fields drifted");
    if (item >= 0 && (input.readUnsignedByte() != 1 || input.readShort() != 0))
      throw new AssertionError("armor Packet102 stack drifted");
  }
}
