package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteNoteEvent;

/** Public Packet54/61 note-event boundary kept out of the capped play client. */
public final class B173NoteAccess {
    private B173NoteAccess() {}

    public static RemoteNoteEvent await(B173WireClient client) {
        try { return client.channel().awaitNoteEvent(); }
        catch (IOException error) { throw new IllegalStateException("note event receive failed", error); }
    }

    public static RemoteNoteEvent poll(B173WireClient client) {
        return client.channel().inbound().notes().take();
    }
}
