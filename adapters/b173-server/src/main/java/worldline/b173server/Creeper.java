package worldline.b173server;

import worldline.api.RemoteExplosion;
import worldline.api.RemoteMobSpawn;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineEvidence;

/** Public creeper verbs. Official fuse-then-explode is stay in range, not a cheat. */
public final class Creeper {
    private Creeper() {}

    public static RemoteExplosion stayUntilExplode(B173WireClient actor, RemoteMobSpawn creeper) {
        return B173CreeperFuseAccess.stayThenExplode(actor, creeper);
    }

    public static WorldlineEvidence evidence(String signal, String signature) {
        return WorldlineEvidence.of(WorldlineBehavior.CREEPER_FUSE, WorldlineEvidence.VANILLA, signal, signature);
    }
}
