package worldline.smoke.spawnlightcapsetb173;

import worldline.api.BlockPosition;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteMobSpawn;
import worldline.b173server.B173HostileAccess;
import worldline.b173server.B173WireClient;

/** Nearby Packet24 50/54 probe: present on the unlit pad, rejected under sparse torch light >= 8. */
final class SpawnLightCapProbe {
    private SpawnLightCapProbe() {}

    static boolean near(RemoteMobSpawn spawn, BlockPosition first, BlockPosition second) {
        return inside(spawn, first) || inside(spawn, second);
    }

    static boolean inside(RemoteMobSpawn spawn, BlockPosition position) {
        return Math.abs(spawn.x() - (position.x() + 0.5D)) <= 4.5D
                && Math.abs(spawn.y() - position.y()) <= 2D
                && Math.abs(spawn.z() - (position.z() + 0.5D)) <= 4.5D;
    }

    static RemoteMobSpawn awaitDark(B173WireClient actor, BlockPosition first, BlockPosition second) {
        int player = actor.state().entityId();
        for (int n = 0; n < 64; n++) {
            RemoteMobSpawn spawn = B173HostileAccess.next(actor);
            int type = spawn.legacyType();
            SpawnLightCapPad.require(spawn.entityId() != player && type != 90
                    && (type == 50 || type == 51 || type == 52 || type == 54),
                    "dark Packet24 identity drift");
            if ((type == 50 || type == 54) && near(spawn, first, second)) return spawn;
        }
        throw new IllegalStateException("dark Packet24 type 50 or 54 absent near pad");
    }

    static void requireTorchReject(B173WireClient actor, BlockPosition first, BlockPosition second,
            int ticks) {
        worldline.test.WorldlineSmokeAwait.observe(actor, ticks);
        RemoteMobSpawn peek = B173HostileAccess.peekDespawnFamily(actor);
        while (peek != null) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(peek.legacyType());
            SpawnLightCapPad.require(!((spawn.legacyType() == 50 || spawn.legacyType() == 54)
                    && near(spawn, first, second)),
                    "torch Packet24 type " + spawn.legacyType() + " near pad");
            peek = B173HostileAccess.peekDespawnFamily(actor);
        }
    }

    static int blockLight(B173WireClient actor, BlockPosition world, int cx, int cz) {
        RemoteChunkSnapshot chunk = worldline.test.WorldlineSmokeAwait.observe(actor, 5).chunkAt(cx, cz);
        return chunk.blockLightAt(world.x() - cx * 16, world.y(), world.z() - cz * 16);
    }
}
