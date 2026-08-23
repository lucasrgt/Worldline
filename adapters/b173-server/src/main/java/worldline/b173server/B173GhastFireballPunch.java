package worldline.b173server;

import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** Reusable Packet7 punch that redirects a ghast Packet23 type-63 via Packet28. */
public final class B173GhastFireballPunch {
    private B173GhastFireballPunch() {}

    public static B173EntityVelocity take(B173WireClient actor, int entityId) {
        return actor.channel().inbound().velocities().take(entityId);
    }

    public static boolean redirectedUp(B173EntityVelocity velocity) {
        return velocity != null && velocity.y() >= 0.5D
                && Math.abs(velocity.x()) < 0.4D && Math.abs(velocity.z()) < 0.4D;
    }

    public static B173EntityVelocity awaitRedirect(B173WireClient actor, RemoteMobSpawn ghast) {
        int shot = 0;
        while (shot < 4) {
            RemoteObjectSpawn ball = B173GhastFireballHit.fireball(actor, ghast);
            B173EntityVelocity velocity = punchOne(actor, ball);
            if (velocity != null) return velocity;
            if (actor.health() <= 0)
                throw new IllegalStateException("ghast fireball punch killed the actor");
            shot++;
        }
        throw new IllegalStateException("ghast fireball punch Packet28 redirect absent");
    }

    private static B173EntityVelocity punchOne(B173WireClient actor, RemoteObjectSpawn ball) {
        if (ball == null || ball.type() != 63 || ball.entityId() == actor.state().entityId())
            throw new IllegalStateException("ghast fireball punch identity drift");
        int health = actor.health();
        int tick = 0;
        while (tick < 80) {
            actor.look(0.0F, -90.0F);
            actor.attackMob(ball.entityId());
            actor.sustainTicks(1);
            B173EntityVelocity velocity = take(actor, ball.entityId());
            if (redirectedUp(velocity)) return velocity;
            if (actor.health() < health) return null;
            tick++;
        }
        return null;
    }
}
