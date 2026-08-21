package worldline.b173server;

import java.io.IOException;
import worldline.api.RemotePaintingSpawn;

/** Public Packet25 painting-spawn boundary kept out of the capped play client. */
public final class B173PaintingAccess {
    private B173PaintingAccess() {}

    public static RemotePaintingSpawn await(B173WireClient client) {
        try { return client.channel().awaitPainting(); }
        catch (IOException error) { throw new IllegalStateException("painting spawn receive failed", error); }
    }
}
