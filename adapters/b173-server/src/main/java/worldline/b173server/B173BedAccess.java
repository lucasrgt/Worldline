package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteBedUse;

/** Public Packet17 sleep boundary kept out of the capped play client. */
public final class B173BedAccess {
    private B173BedAccess() {}

    public static RemoteBedUse await(B173WireClient client) {
        try { return client.channel().awaitBedUse(); }
        catch (IOException error) { throw new IllegalStateException("bed use receive failed", error); }
    }
}
