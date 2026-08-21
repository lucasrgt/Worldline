package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import worldline.api.BlockPosition;
import worldline.api.RemoteNoteEvent;

/** Bounded Packet54/61 decoder. Packet54 is 12 bytes; Packet61 is 17 live bytes. */
final class B173NoteEvent {
    private static final int MAX = 64;
    private final ArrayList<RemoteNoteEvent> pending = new ArrayList<RemoteNoteEvent>();

    void accept(int packet, DataInputStream input) throws IOException {
        try {
            if (packet == 54) {
                int x = input.readInt(), y = input.readShort(), z = input.readInt();
                offer(new RemoteNoteEvent(54, new BlockPosition(x, y, z),
                        input.readUnsignedByte(), input.readUnsignedByte()));
                return;
            }
            if (packet == 61) {
                int effect = input.readInt(), x = input.readInt(), y = input.readByte();
                int z = input.readInt(), data = input.readInt();
                offer(new RemoteNoteEvent(61, new BlockPosition(x, y, z), effect, data));
                return;
            }
        } catch (IllegalArgumentException error) {
            throw new IOException("invalid note event", error);
        }
        throw new IOException("unexpected note packet " + packet);
    }

    RemoteNoteEvent take() {
        return pending.isEmpty() ? null : pending.remove(0);
    }

    private void offer(RemoteNoteEvent event) {
        if (pending.size() == MAX) pending.remove(0);
        pending.add(event);
    }
}
