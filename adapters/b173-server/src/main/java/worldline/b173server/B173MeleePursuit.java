package worldline.b173server;

import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Reusable Packet24 identity and Packet31/33/34 pursuit-vector boundary. */
public final class B173MeleePursuit {
    private static final int GOLDEN_APPLE = 322;

    private B173MeleePursuit() {}

    public static RemoteMobSpawn near(B173WireClient actor, int type, BlockPosition spawner) {
        for (int count = 0; count < 32; count++) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(type);
            double dx = spawn.x() - (spawner.x() + 0.5D);
            double dz = spawn.z() - (spawner.z() + 0.5D);
            if (dx * dx + dz * dz <= 100D && Math.abs(spawn.y() - spawner.y()) <= 6D) return spawn;
        }
        throw new IllegalStateException("nearby hostile type " + type + " absent");
    }

    public static RemoteMobMovement toward(B173WireClient actor, int entity) {
        PlayerPose pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        for (int count = 0; count < 128; count++) {
            heal(actor);
            if (actor.health() <= 0) throw new IllegalStateException("actor died during melee pursuit");
            RemoteMobMovement movement = actor.awaitMobMovement(entity);
            double mx = movement.toX() - movement.fromX(), mz = movement.toZ() - movement.fromZ();
            double px = pose.x() - movement.fromX(), pz = pose.z() - movement.fromZ();
            if ((mx != 0D || mz != 0D) && mx * px + mz * pz > 0D) return movement;
        }
        throw new IllegalStateException("pursuit vector toward pose absent for " + entity);
    }

    public static void heal(B173WireClient actor) {
        int health = actor.health();
        if (health == 0) throw new IllegalStateException("actor died during melee pursuit");
        if (health >= 20) return;
        int food = find(actor.inventory(), GOLDEN_APPLE);
        if (food < 36) return;
        actor.selectHeldSlot(food - 36);
        actor.useSelectedItemInAir();
        actor.sustainTicks(5);
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        return -1;
    }
}
