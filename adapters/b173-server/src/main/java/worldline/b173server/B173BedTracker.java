package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteBedUse;

/** Bounded decoder for protocol-14 Packet17 sleep and Packet70Bed. */
final class B173BedTracker {
    private RemoteBedUse sleep;
    private int packet70 = RemoteBedUse.NO_PACKET70;

    void accept(int packet, DataInputStream input) throws IOException {
        if (packet == 17) {
            if (sleep != null) throw new IOException("unconsumed Packet17 sleep");
            int entity = input.readInt(), unused = input.readByte();
            int x = input.readInt(), y = input.readByte() & 255, z = input.readInt();
            sleep = new RemoteBedUse(entity, unused, x, y, z, packet70);
            return;
        }
        if (packet != 70) throw new IOException("unexpected bed packet " + packet);
        int reason = input.readUnsignedByte();
        if (reason > 2) throw new IOException("invalid Packet70 reason " + reason);
        packet70 = reason;
    }

    RemoteBedUse takeSleep() {
        RemoteBedUse value = sleep;
        sleep = null;
        return value;
    }
}
