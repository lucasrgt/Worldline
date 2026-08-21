package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import worldline.api.RemoteItemStack;

/** Byte oracle for the first Packet102 family craft: wooden sword 268. */
public final class B173WoodToolPacketFixture {
    private B173WoodToolPacketFixture() {}

    public static void verify() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        B173ContainerPacket.write(output, 1, 40, 0, 1, new RemoteItemStack(280, 9, 0));
        B173ContainerPacket.write(output, 1, 8, 1, 2, null);
        B173ContainerPacket.write(output, 1, 40, 0, 3, null);
        B173ContainerPacket.write(output, 1, 39, 0, 4, new RemoteItemStack(5, 11, 0));
        B173ContainerPacket.write(output, 1, 2, 1, 5, null);
        B173ContainerPacket.write(output, 1, 5, 1, 6, null);
        B173ContainerPacket.write(output, 1, 39, 0, 7, null);
        B173ContainerPacket.write(output, 1, 0, 0, 8, new RemoteItemStack(268, 1, 0));
        B173ContainerPacket.write(output, 1, 10, 0, 9, null);
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        packet(input, 40, 0, 1, 280, 9); packet(input, 8, 1, 2, -1, 0);
        packet(input, 40, 0, 3, -1, 0); packet(input, 39, 0, 4, 5, 11);
        packet(input, 2, 1, 5, -1, 0); packet(input, 5, 1, 6, -1, 0);
        packet(input, 39, 0, 7, -1, 0); packet(input, 0, 0, 8, 268, 1);
        packet(input, 10, 0, 9, -1, 0);
        if (input.available() != 0 || bytes.size() != 99)
            throw new AssertionError("wood-tool Packet102 sequence size drifted");
    }

    private static void packet(DataInputStream input, int slot, int button, int action, int item, int count)
            throws Exception {
        if (input.readUnsignedByte() != 102 || input.readUnsignedByte() != 1 || input.readShort() != slot
                || input.readUnsignedByte() != button || input.readShort() != action || input.readBoolean()
                || input.readShort() != item)
            throw new AssertionError("wood-tool Packet102 fields drifted");
        if (item >= 0 && (input.readUnsignedByte() != count || input.readShort() != 0))
            throw new AssertionError("wood-tool Packet102 stack drifted");
    }
}
