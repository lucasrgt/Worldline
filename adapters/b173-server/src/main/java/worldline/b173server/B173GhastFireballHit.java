package worldline.b173server;

import worldline.api.BlockPosition;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;
import worldline.api.RemoteWorldView;

/** Reusable ghast Packet23 type-63 and Packet60 strength-one impact boundary. */
public final class B173GhastFireballHit {
    private B173GhastFireballHit() {}

    public static RemoteObjectSpawn fireball(B173WireClient actor, RemoteMobSpawn ghast) {
        RemoteObjectSpawn ball = actor.awaitObjectSpawn(63);
        if (ghast.legacyType() != 56 || ball.type() != 63 || ball.entityId() == ghast.entityId()
                || ball.entityId() == actor.state().entityId() || ball.throwerId() != ghast.entityId())
            throw new IllegalStateException("ghast fireball Packet23 identity drift");
        return ball;
    }

    public static RemoteExplosion awaitImpact(B173WireClient actor, RemoteWorldView before, BlockPosition target) {
        for (int count = 0; count < 12; count++) {
            RemoteExplosion explosion = actor.awaitExplosion();
            if (explosion.strength() != 1F)
                throw new IllegalStateException("ghast fireball Packet60 strength drift");
            boolean hurt = actor.health() < 20;
            boolean lava = lavaAt(actor.sustainTicks(1), target);
            int crater = cratered(before, explosion, 87) + cratered(before, explosion, 4);
            int near = nearby(explosion, target);
            if ((hurt && !lava) || crater > 0 || near > 0) return explosion;
            if (count == 11) throw new IllegalStateException("ghast fireball impact absent");
        }
        throw new IllegalStateException("ghast fireball impact loop exhausted");
    }

    private static int cratered(RemoteWorldView before, RemoteExplosion explosion, int id) {
        int count = 0;
        for (BlockPosition position : explosion.destroyed()) {
            if (!before.containsChunk(Math.floorDiv(position.x(), 16), Math.floorDiv(position.z(), 16))) continue;
            if (before.blockAt(position.x(), position.y(), position.z()).legacyId() == id) count++;
        }
        return count;
    }

    private static int nearby(RemoteExplosion explosion, BlockPosition target) {
        int count = 0;
        for (BlockPosition position : explosion.destroyed())
            if (Math.abs(position.x() - target.x()) <= 8 && Math.abs(position.y() - target.y()) <= 8
                    && Math.abs(position.z() - target.z()) <= 8) count++;
        return count;
    }

    private static boolean lavaAt(RemoteWorldView view, BlockPosition target) {
        for (int dy = 0; dy <= 2; dy++) {
            if (!view.containsChunk(Math.floorDiv(target.x(), 16), Math.floorDiv(target.z(), 16))) continue;
            int id = view.blockAt(target.x(), target.y() + dy, target.z()).legacyId();
            if (id == 10 || id == 11) return true;
        }
        return false;
    }
}
