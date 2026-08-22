package worldline.b173server;

import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Reusable Packet24 spider take, leap-toward-actor, and Packet8 touch boundary. */
public final class B173SpiderLeapAccess {
    public static final int TYPE = 52, GOLDEN = 322, CAP = 9;

    private B173SpiderLeapAccess() {}

    public static RemoteMobSpawn near(B173WireClient actor, BlockPosition spawner) {
        for (int n = 0; n < 32; n++) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(TYPE);
            double dx = spawn.x() - (spawner.x() + 0.5D), dz = spawn.z() - (spawner.z() + 0.5D);
            if (dx * dx + dz * dz <= 36D && Math.abs(spawn.y() - spawner.y()) <= 2.5D
                    && spawn.legacyType() == TYPE && spawn.entityId() != actor.state().entityId())
                return spawn;
        }
        throw new IllegalStateException("nearby spider type 52 absent");
    }

    public static void step(B173WireClient actor, double x, double y, double z, double reach) {
        for (int n = 0; n < 16; n++) {
            PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= reach) return;
            double scale = Math.min(1D, CAP / dist);
            actor.moveAndObserve(dx * scale, dy * scale, dz * scale, 2);
        }
    }

    public static RemoteIncomingHit leapAndHurt(B173WireClient actor, RemoteMobSpawn spider, BlockPosition spawner) {
        int entity = spider.entityId();
        double x = spider.x(), z = spider.z();
        boolean leaped = false;
        RemoteIncomingHit hit = null;
        hold(actor, spawner, x, z);
        for (int n = 0; n < 320; n++) {
            PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            RemoteMobMovement move = actor.awaitMobMovement(entity);
            x = move.toX(); z = move.toZ();
            double before = hypot(move.fromX() - here.x(), move.fromZ() - here.z());
            double after = hypot(move.toX() - here.x(), move.toZ() - here.z());
            int rise = move.toFixedY() - move.fromFixedY();
            if (rise >= 4 && after + 0.05D < before && (move.packetId() == 31 || move.packetId() == 33))
                leaped = true;
            int health = actor.health();
            if (health == 0) throw new IllegalStateException("actor died during spider leap");
            if (hit == null && health >= 1 && health < 20) hit = takeHit(actor, health);
            if (leaped && hit != null) return hit;
            if (!leaped) hold(actor, spawner, x, z);
        }
        if (!leaped) throw new IllegalStateException("spider leap toward actor absent");
        if (hit != null) return hit;
        return takeHit(actor, 18);
    }

    public static void heal(B173WireClient actor) {
        int health = actor.health();
        if (health == 0) throw new IllegalStateException("actor died during spider leap");
        if (health >= 20) return;
        int food = find(actor.inventory(), GOLDEN);
        if (food < 36) throw new IllegalStateException("golden apple 322 lost");
        actor.selectHeldSlot(food - 36);
        actor.useSelectedItemInAir();
        actor.sustainTicks(5);
        if (actor.awaitHealth(20) != 20) throw new IllegalStateException("golden apple 322 heal drift");
    }

    private static RemoteIncomingHit takeHit(B173WireClient actor, int expected) {
        RemoteIncomingHit hit = actor.awaitIncomingHit(expected);
        if (hit.healthBefore() <= hit.healthAfter() || hit.damage() < 1)
            throw new IllegalStateException("spider Packet8 touch drift");
        return hit;
    }

    private static void hold(B173WireClient actor, BlockPosition spawner, double sx, double sz) {
        PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        double dx = here.x() - sx, dz = here.z() - sz, dist = Math.sqrt(dx * dx + dz * dz);
        if (dist >= 2.2D && dist <= 5.0D) return;
        if (dist < 0.05D) { dx = 0D; dz = -1D; dist = 1D; }
        double scale = 3.0D / dist;
        double tx = clamp(sx + dx * scale, spawner.x() - 2.5D, spawner.x() + 3.5D);
        double tz = clamp(sz + dz * scale, spawner.z() - 2.5D, spawner.z() + 3.5D);
        step(actor, tx, here.y(), tz, 0.75D);
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        return -1;
    }

    private static double hypot(double x, double z) { return Math.sqrt(x * x + z * z); }
    private static double clamp(double v, double lo, double hi) { return v < lo ? lo : (v > hi ? hi : v); }
}
