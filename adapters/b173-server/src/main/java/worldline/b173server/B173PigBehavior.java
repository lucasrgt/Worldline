package worldline.b173server;

import worldline.api.PlayerPose;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Reusable Packet24 pig identity, movement, death, and pork-drop boundary. */
public final class B173PigBehavior {
    private static final int PIG_TYPE = 90;
    private static final RemoteItemStack PORK = new RemoteItemStack(319, 1, 0);

    private B173PigBehavior() {}

    public static RemoteMobSpawn spawn(B173WireClient client) {
        RemoteMobSpawn spawn = client.awaitMobSpawn(PIG_TYPE);
        if (spawn.legacyType() != PIG_TYPE || spawn.entityId() <= 0)
            throw new IllegalStateException("positive Packet24 pig identity absent");
        return spawn;
    }

    public static RemoteMobMovement horizontal(B173WireClient client, int entity) {
        for (int count = 0; count < 128; count++) {
            RemoteMobMovement movement = client.awaitMobMovement(entity);
            if (movement.fromFixedX() != movement.toFixedX()
                    || movement.fromFixedZ() != movement.toFixedZ()) return movement;
        }
        throw new IllegalStateException("horizontal pig movement absent");
    }

    public static RemoteMobMovement observedHorizontal(B173WireClient client) {
        for (int count = 0; count < 128; count++) {
            RemoteMobMovement movement = client.awaitObservedMobMovement();
            if (movement.fromFixedX() != movement.toFixedX()
                    || movement.fromFixedZ() != movement.toFixedZ()) return movement;
        }
        throw new IllegalStateException("horizontal observed pig movement absent");
    }

    public static void approach(B173WireClient client, RemoteMobSpawn spawn) {
        PlayerPose pose = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
        double dx = spawn.x() - pose.x(), dz = spawn.z() - pose.z();
        double distance = Math.hypot(dx, dz);
        if (distance < 1D) client.moveAndObserve(2D, 0D, 0D, 8);
        else if (distance > 2.5D) {
            double scale = (distance - 2D) / distance;
            client.moveAndObserve(dx * scale, 0D, dz * scale, 10);
        }
    }

    public static RemoteMobDeath death(B173WireClient client, int entity) {
        RemoteMobDeath death = client.awaitMobDeath(entity);
        if (death.entityId() != entity || death.deathStatus() != 3 || death.destroyPacket() != 29)
            throw new IllegalStateException("pig Packet38/29 death boundary drift");
        return death;
    }

    public static RemoteDroppedItem pork(B173WireClient client) {
        RemoteDroppedItem item = client.peekDroppedItem(PORK);
        if (item != null && item.item().legacyId() != 319)
            throw new IllegalStateException("pig pork Packet21 identity drift");
        return item;
    }
}
