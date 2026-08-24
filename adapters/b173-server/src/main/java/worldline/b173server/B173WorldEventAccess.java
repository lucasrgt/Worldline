package worldline.b173server;

import worldline.api.BlockPosition;
import worldline.api.RemoteNoteEvent;
import worldline.api.RemoteWorldEvent;

/** Public semantic Packet61 boundary over the shared protocol-14 event decoder. */
public final class B173WorldEventAccess {
    private B173WorldEventAccess() { }

    public static RemoteWorldEvent await(B173WireClient client,
            int effectId, BlockPosition position) {
        if (client == null || effectId < 0 || position == null)
            throw new IllegalArgumentException("invalid expected world event");
        for (int count = 0; count < 64; count++) {
            RemoteWorldEvent value = convert(B173NoteAccess.await(client));
            if (value != null && value.effectId() == effectId && value.position().equals(position))
                return value;
        }
        throw new IllegalStateException("expected world event absent from bounded event window");
    }

    public static RemoteWorldEvent poll(B173WireClient client) {
        if (client == null) throw new IllegalArgumentException("null world-event client");
        RemoteNoteEvent value;
        while ((value = B173NoteAccess.poll(client)) != null) {
            RemoteWorldEvent event = convert(value);
            if (event != null) return event;
        }
        return null;
    }

    private static RemoteWorldEvent convert(RemoteNoteEvent value) {
        return value != null && value.packetId() == 61
                ? new RemoteWorldEvent(value.position(), value.instrument(), value.pitch()) : null;
    }
}
