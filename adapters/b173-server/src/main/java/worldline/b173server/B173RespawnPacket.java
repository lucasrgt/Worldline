package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;

/** Exact protocol-14 Packet9 death-respawn request encoder. */
final class B173RespawnPacket {
    private B173RespawnPacket() {}
    static void write(DataOutputStream output, int dimension) throws IOException {
        if (dimension != 0 && dimension != -1) throw new IllegalArgumentException("invalid respawn dimension");
        output.writeByte(9); output.writeByte(dimension);
    }
}
