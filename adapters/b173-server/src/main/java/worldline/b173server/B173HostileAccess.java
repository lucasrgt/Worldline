package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteMobSpawn;

/** Adapter-local Packet24 wait for the Overworld hostile identity family. */
public final class B173HostileAccess {
    private static final int[] FAMILY = {50, 51, 52, 54};

    private B173HostileAccess() {}

    public static RemoteMobSpawn next(B173WireClient actor) {
        try { return actor.channel().inbound().awaitMobSpawnAny(FAMILY); }
        catch (IOException error) { throw new IllegalStateException("hostile spawn receive failed", error); }
    }
}
