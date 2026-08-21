package worldline.b173server;

import java.io.*;

/** Freezes the production Nether Packet9 death-respawn request. */
public final class B173NetherRespawnPacketFixture {
    private B173NetherRespawnPacketFixture() {}
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream output = new DataOutputStream(bytes);
        B173RespawnPacket.write(output, -1); byte[] value = bytes.toByteArray();
        if (value.length != 2 || value[0] != 9 || value[1] != (byte) 255) throw new IllegalStateException("Nether Packet9 encoding drift");
        System.out.println("WORLDLINE_M136_PACKET9=09-ff");
    }
}
