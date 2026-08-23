package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/** Byte oracle proving the exact right-place Packet102 fields used by M63. */
public final class B173WorkbenchPacketFixture {
  private B173WorkbenchPacketFixture() {
  }
  public static void verify() throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream output = new DataOutputStream(bytes);
    for (int slot = 1; slot <= 3; slot++)
      B173ContainerPacket.write(output, 7, slot, 1, slot + 1, null);
    DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    for (int slot = 1; slot <= 3; slot++)
      if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 7
          || input.readShort() != slot || input.readUnsignedByte() != 1
          || input.readShort() != slot + 1 || input.readBoolean() || input.readShort() != -1)
        throw new AssertionError("workbench right-place Packet102 encoding drifted");
    if (input.available() != 0 || bytes.size() != 30)
      throw new AssertionError("workbench right-place Packet102 size drifted");
  }
}
