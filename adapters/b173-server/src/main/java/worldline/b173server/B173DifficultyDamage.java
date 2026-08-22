package worldline.b173server;

import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteMobSpawn;

/** Reusable nearby-zombie and bounded melee Packet8 observation. */
public final class B173DifficultyDamage {
    private B173DifficultyDamage() {}

    public static RemoteMobSpawn near(B173WireClient actor, BlockPosition spawner) {
        for (int count = 0; count < 32; count++) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(54);
            double dx = spawn.x() - (spawner.x() + 0.5D), dz = spawn.z() - (spawner.z() + 0.5D);
            if (dx * dx + dz * dz <= 36D && Math.abs(spawn.y() - spawner.y()) <= 3D) return spawn;
        }
        throw new IllegalStateException("nearby zombie type 54 absent");
    }

    public static RemoteIncomingHit strike(B173WireClient actor, RemoteMobSpawn zombie, BlockPosition station) {
        if (zombie.legacyType() != 54 || zombie.entityId() == actor.state().entityId())
            throw new IllegalArgumentException("invalid zombie strike subject");
        station(actor, station);
        for (int count = 0; count < 80; count++) {
            if (actor.health() < 20) return actor.awaitIncomingHit(actor.health());
            station(actor, station);
            actor.sustainTicks(5);
        }
        throw new IllegalStateException("zombie melee Packet8 absent");
    }

    public static void station(B173WireClient actor, BlockPosition top) {
        PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        actor.moveAndObserve(top.x() + 0.5D - here.x(), top.y() + 1.1D - here.y(),
                top.z() - 1.5D - here.z(), 9);
    }
}
