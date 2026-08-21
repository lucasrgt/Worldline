package worldline.b173server;

import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** Aims bow 261 and air-uses until Packet23 type 60 produces Packet38 status 2 on an armed mob. */
public final class B173BowMobHit {
    private B173BowMobHit() {}

    public static RemoteMobSpawn near(B173WireClient actor, int type, double x, double y, double z) {
        for (int n = 0; n < 32; n++) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(type);
            double dx = spawn.x() - x, dy = spawn.y() - y, dz = spawn.z() - z;
            if (dx * dx + dz * dz <= 100D && Math.abs(dy) <= 6D) return spawn;
        }
        throw new IllegalStateException("nearby Packet24 type " + type + " absent");
    }

    public static RemoteObjectSpawn shoot(B173WireClient actor, RemoteMobSpawn spawn) {
        int entity = spawn.entityId(), actorId = actor.state().entityId();
        double x = spawn.x(), y = spawn.y(), z = spawn.z();
        RemoteObjectSpawn arrow = null;
        int bow = find(actor.inventory(), 261);
        if (bow < 36) throw new IllegalStateException("bow 261 lost");
        actor.selectHeldSlot(bow - 36);
        drainArrows(actor);
        for (int shot = 0; shot < 8; shot++) {
            heal(actor);
            if (hurt(actor, entity)) break;
            RemoteMobMovement move = actor.channel().inbound().mobs().takeMovement(entity);
            if (move != null) { x = move.toX(); y = move.toY(); z = move.toZ(); }
            double body = spawn.legacyType() == 90 ? 0.45D : 0.9D;
            approach(actor, x, y + 1.0D, z, 1.8D);
            aim(actor, x, y + body, z);
            bow = find(actor.inventory(), 261);
            if (bow < 36) throw new IllegalStateException("bow 261 lost");
            actor.selectHeldSlot(bow - 36);
            drainArrows(actor);
            actor.useSelectedItemInAir();
            arrow = playerArrow(actor, actorId);
            for (int n = 0; n < 8; n++) {
                if (hurt(actor, entity)) return arrow;
                actor.sustainTicks(3);
                move = actor.channel().inbound().mobs().takeMovement(entity);
                if (move != null) { x = move.toX(); y = move.toY(); z = move.toZ(); }
            }
        }
        if (!hurt(actor, entity)) throw new IllegalStateException("Packet38 status 2 absent for type " + spawn.legacyType());
        finishDeath(actor, entity);
        return arrow;
    }

    public static void approach(B173WireClient actor, double x, double y, double z, double reach) {
        for (int step = 0; step < 16; step++) {
            heal(actor);
            PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= reach) return;
            double s = Math.min(1D, 9.0D / dist);
            actor.moveAndObserve(dx * s, dy * s, dz * s, 2);
        }
    }

    static void aim(B173WireClient actor, double x, double y, double z) {
        PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        double dx = x - here.x(), dy = y - (here.y() + 1.62D), dz = z - here.z();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = horiz < 1.0E-4D ? here.yaw() : (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90F;
        float pitch = horiz < 1.0E-4D ? here.pitch() : (float) (-(Math.atan2(dy, horiz) * 180.0D / Math.PI));
        if (pitch < -90F) pitch = -90F;
        if (pitch > 90F) pitch = 90F;
        actor.look(yaw, pitch);
    }

    static void heal(B173WireClient actor) {
        int health = actor.health();
        if (health == 0) throw new IllegalStateException("actor died during bow-mob-hit");
        if (health >= 20) return;
        int food = find(actor.inventory(), 322);
        if (food < 36) food = find(actor.inventory(), 320);
        if (food < 36) return;
        actor.selectHeldSlot(food - 36);
        actor.useSelectedItemInAir();
        actor.sustainTicks(5);
    }

    static boolean hurt(B173WireClient actor, int entity) {
        if (B173ShearsAccess.peekHurt(actor, entity)) {
            finishDeath(actor, entity);
            return true;
        }
        return B173ShearsAccess.peekDeath(actor, entity) != null && finishDeath(actor, entity);
    }

    static boolean finishDeath(B173WireClient actor, int entity) {
        RemoteMobDeath death = B173ShearsAccess.peekDeath(actor, entity);
        if (death == null) return false;
        RemoteMobDeath observed = actor.awaitMobDeath(entity);
        if (observed.entityId() != entity || !observed.hurtObserved())
            throw new IllegalStateException("Packet38 status 2 missing on death");
        return true;
    }

    static void drainArrows(B173WireClient actor) {
        while (actor.channel().inbound().objects().take(60) != null) { }
    }

    static RemoteObjectSpawn playerArrow(B173WireClient actor, int actorId) {
        RemoteObjectSpawn arrow = actor.awaitObjectSpawn(60);
        if (arrow.type() == 60 && (arrow.throwerId() == actorId || arrow.throwerId() == 0)) return arrow;
        arrow = actor.awaitObjectSpawn(60);
        if (arrow.type() != 60 || (arrow.throwerId() != actorId && arrow.throwerId() != 0))
            throw new IllegalStateException("player Packet23 type 60 thrower drift type=" + arrow.type()
                    + ",thrower=" + arrow.throwerId() + ",actor=" + actorId);
        return arrow;
    }

    static int find(RemoteInventoryView view, int id) {
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        return -1;
    }
}
