package worldline.b173server;

import java.io.IOException;
import worldline.api.RemotePaintingSpawn;

/** Public Packet25 spawn and Packet29 destroy boundaries kept out of the capped play client. */
public final class B173PaintingAccess {
    private B173PaintingAccess() {}

    public static RemotePaintingSpawn await(B173WireClient client) {
        try { return client.channel().awaitPainting(); }
        catch (IOException error) { throw new IllegalStateException("painting spawn receive failed", error); }
    }

    public static Integer peekDestroy(B173WireClient client, int entityId) {
        if (entityId < 0) throw new IllegalArgumentException("invalid painting entity id");
        return client.channel().inbound().paintings().takeDestroy(entityId);
    }

    public static int awaitDestroy(B173WireClient client, int entityId) {
        try { return client.channel().inbound().awaitPaintingDestroy(entityId).intValue(); }
        catch (IOException error) { throw new IllegalStateException("painting destroy receive failed", error); }
    }
}
