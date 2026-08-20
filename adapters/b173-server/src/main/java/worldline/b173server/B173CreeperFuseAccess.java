package worldline.b173server;

import java.io.IOException;
import worldline.api.PlayerPose;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteMobSpawn;

/** Adapter-local proximity stay plus Packet40 ignited state before Packet60. */
public final class B173CreeperFuseAccess {
    private B173CreeperFuseAccess() {}

    public static RemoteExplosion stayThenExplode(B173WireClient actor, RemoteMobSpawn creeper) {
        if (actor == null || creeper == null || creeper.legacyType() != 50)
            throw new IllegalArgumentException("invalid creeper fuse stay");
        try {
            closeIn(actor, creeper);
            B173PlayInbound inbound = actor.channel().inbound();
            inbound.awaitCreeperFuse(creeper.entityId());
            if (!inbound.explosionQueued()) actor.sustainTicks(40);
            return actor.awaitExplosion();
        } catch (IOException error) {
            throw new IllegalStateException("creeper fuse-then-explode absent", error);
        }
    }

    private static void closeIn(B173WireClient actor, RemoteMobSpawn creeper) {
        double x = creeper.x(), y = creeper.y(), z = creeper.z();
        for (int n = 0; n < 4; n++) {
            PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= 3.0D || dist > 9.0D) return;
            double s = Math.min(1D, 9.0D / dist);
            actor.moveAndObserve(dx * s, dy * s, dz * s, 2);
        }
    }
}
