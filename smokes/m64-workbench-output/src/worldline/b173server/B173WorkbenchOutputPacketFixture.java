package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import worldline.api.RemoteItemStack;

/** Byte oracle for exact slabs output take and personal-store Packet102 requests. */
public final class B173WorkbenchOutputPacketFixture {
  private B173WorkbenchOutputPacketFixture() {
  }
  public static void verify() throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    B173ContainerPacket.write(out, 7, 0, 0, 5, new RemoteItemStack(44, 3, 2));
    B173ContainerPacket.write(out, 7, 37, 0, 6, null);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    require(in.readUnsignedByte() == 102 && in.readUnsignedByte() == 7 && in.readShort() == 0
        && in.readUnsignedByte() == 0 && in.readShort() == 5 && !in.readBoolean()
        && in.readShort() == 44 && in.readUnsignedByte() == 3 && in.readShort() == 2);
    require(in.readUnsignedByte() == 102 && in.readUnsignedByte() == 7 && in.readShort() == 37
        && in.readUnsignedByte() == 0 && in.readShort() == 6 && !in.readBoolean()
        && in.readShort() == -1);
    require(in.available() == 0 && bytes.size() == 23);
  }
  private static void require(boolean value) {
    if (!value)
      throw new AssertionError("workbench output wire drifted");
  }
}
