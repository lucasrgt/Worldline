package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import worldline.api.BlockPosition;
import worldline.api.RemoteSignText;

/** Bounded Packet130 decoder. Payload is x int, y short, z int, then four UCS-2 lines. */
final class B173SignTracker {
    private static final int MAX = 64;
    private final ArrayList<RemoteSignText> pending = new ArrayList<RemoteSignText>();

    void accept(DataInputStream input) throws IOException {
        try {
            int x = input.readInt(), y = input.readShort(), z = input.readInt();
            String line0 = B173InboundPacket.string(input, 15);
            String line1 = B173InboundPacket.string(input, 15);
            String line2 = B173InboundPacket.string(input, 15);
            String line3 = B173InboundPacket.string(input, 15);
            offer(new RemoteSignText(new BlockPosition(x, y, z), line0, line1, line2, line3));
        } catch (IllegalArgumentException error) {
            throw new IOException("invalid sign text", error);
        }
    }

    RemoteSignText take() {
        return pending.isEmpty() ? null : pending.remove(0);
    }

    private void offer(RemoteSignText text) {
        if (pending.size() == MAX) pending.remove(0);
        pending.add(text);
    }
}
