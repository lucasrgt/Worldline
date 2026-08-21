package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** Adapter-local Packet24 wait for the Overworld hostile identity family. */
public final class B173HostileAccess {
    private static final int[] FAMILY = {50, 51, 52, 54};
    private static final int[] DESPAWN = {50, 51, 54};

    private B173HostileAccess() {}

    public static RemoteMobSpawn next(B173WireClient actor) {
        try { return actor.channel().inbound().awaitMobSpawnAny(FAMILY); }
        catch (IOException error) { throw new IllegalStateException("hostile spawn receive failed", error); }
    }

    public static RemoteMobSpawn skeleton(B173WireClient actor) {
        try { return actor.channel().inbound().awaitMobSpawn(51); }
        catch (IOException error) { throw new IllegalStateException("skeleton spawn receive failed", error); }
    }

    public static RemoteObjectSpawn arrow(B173WireClient actor, int thrower) {
        try { return actor.channel().inbound().awaitThrownObject(60, thrower); }
        catch (IOException error) { throw new IllegalStateException("skeleton arrow receive failed", error); }
    }

    public static RemoteMobSpawn peekDespawnFamily(B173WireClient actor) {
        B173MobTracker mobs = actor.channel().inbound().mobs();
        for (int type : DESPAWN) {
            RemoteMobSpawn value = mobs.peek(type);
            if (value != null) return value;
        }
        return null;
    }

    public static void requireAbsent(B173WireClient actor, int ticks) {
        actor.sustainTicks(ticks);
        if (peekDespawnFamily(actor) != null)
            throw new IllegalStateException("peaceful Packet24 50/51/54 present");
    }
}
