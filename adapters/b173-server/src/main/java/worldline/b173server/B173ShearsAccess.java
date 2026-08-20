package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteMobDeath;

/** Public smoke boundary for shears Packet7 entity use and a non-blocking death peek. */
public final class B173ShearsAccess {
    private B173ShearsAccess() {}

    public static void useOnMob(B173WireClient client, int entity) {
        try { client.channel().useShearsOnMob(entity); }
        catch (IOException error) { throw new IllegalStateException("shears mob use failed", error); }
    }
    public static void dyeMob(B173WireClient client, int entity) {
        try { client.channel().useDyeOnMob(entity); }
        catch (IOException error) { throw new IllegalStateException("dye mob use failed", error); }
    }

    public static RemoteMobDeath peekDeath(B173WireClient client, int entity) {
        return client.channel().inbound().mobs().peekDeath(entity);
    }
}
