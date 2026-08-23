package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;

/** One decoded protocol-14 Packet30-34 entity motion payload. */
final class B173EntityMove {
    final int packet, entity, x, y, z, yaw, pitch;

    private B173EntityMove(int packet, int entity, int x, int y, int z, int yaw, int pitch) {
        this.packet = packet; this.entity = entity; this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
    }

    static B173EntityMove read(int packet, DataInputStream in) throws IOException {
        if (packet < 30 || packet > 34) throw new IOException("invalid entity move packet");
        int entity = in.readInt(), x = 0, y = 0, z = 0, yaw = -1, pitch = -1;
        if (packet == 31 || packet == 33) { x = in.readByte(); y = in.readByte(); z = in.readByte(); }
        if (packet == 32 || packet == 33) { yaw = in.readUnsignedByte(); pitch = in.readUnsignedByte(); }
        if (packet == 34) {
            x = in.readInt(); y = in.readInt(); z = in.readInt();
            yaw = in.readUnsignedByte(); pitch = in.readUnsignedByte();
        }
        return new B173EntityMove(packet, entity, x, y, z, yaw, pitch);
    }
}
