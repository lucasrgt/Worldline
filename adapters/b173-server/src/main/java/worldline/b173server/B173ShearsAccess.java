package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;

/** Public smoke boundary for shears Packet7 entity use and non-blocking hurt/death peeks. */
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

    public static RemoteMobMovement pollMovement(B173WireClient client, int entity) {
        return client.channel().inbound().mobs().takeMovement(entity);
    }

    public static boolean peekHurt(B173WireClient client, int entity) {
        return client.channel().inbound().mobs().peekHurt(entity);
    }

    public static RemoteDroppedItem wool(B173WireClient client) {
        for (int color = 0; color < 16; color++) {
            RemoteDroppedItem item = client.peekDroppedItem(new RemoteItemStack(35, 1, color));
            if (item != null) return item;
        }
        return null;
    }
}
