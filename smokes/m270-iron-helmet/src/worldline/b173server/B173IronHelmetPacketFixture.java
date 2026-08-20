package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import worldline.api.RemoteItemStack;

/** Byte oracle for the exact two left-click Packet102 messages used by M270. */
public final class B173IronHelmetPacketFixture {
    private B173IronHelmetPacketFixture() {}
    public static void verify() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        B173ContainerPacket.write(output, 0, 36, 0, 1, new RemoteItemStack(306, 1, 0));
        B173ContainerPacket.write(output, 0, 5, 0, 2, null);
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        packet(input, 36, 1, 306); packet(input, 5, 2, -1);
        if (input.available() != 0 || bytes.size() != 23)
            throw new AssertionError("iron helmet Packet102 sequence size drifted");
    }
    private static void packet(DataInputStream input, int slot, int action, int item) throws Exception {
        if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 0 || input.readShort() != slot
                || input.readUnsignedByte() != 0 || input.readShort() != action || input.readBoolean()
                || input.readShort() != item) throw new AssertionError("iron helmet Packet102 fields drifted");
        if (item >= 0 && (input.readUnsignedByte() != 1 || input.readShort() != 0))
            throw new AssertionError("iron helmet Packet102 stack drifted");
    }
}
