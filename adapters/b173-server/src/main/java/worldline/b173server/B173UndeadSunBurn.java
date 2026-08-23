package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteMobSpawn;
import worldline.api.WorldlineHostileBehaviors;
import worldline.api.WorldlineEvidence;

/** Public Packet40 fire-flag wait for official zombie and skeleton sun burn. */
public final class B173UndeadSunBurn {
    private B173UndeadSunBurn() {}

    public static int awaitFire(B173WireClient actor, RemoteMobSpawn spawn) {
        if (actor == null || spawn == null || (spawn.legacyType() != 54 && spawn.legacyType() != 51))
            throw new IllegalArgumentException("invalid undead sun-burn wait");
        try {
            return actor.channel().inbound().awaitMobFire(spawn.entityId());
        } catch (IOException error) {
            throw new IllegalStateException("undead Packet40 fire flag absent", error);
        }
    }

    public static boolean onFire(B173WireClient actor, RemoteMobSpawn spawn) {
        if (actor == null || spawn == null)
            throw new IllegalArgumentException("invalid undead fire probe");
        return actor.channel().inbound().peekMobFire(spawn.entityId());
    }

    public static int flags(B173WireClient actor, RemoteMobSpawn spawn) {
        if (actor == null || spawn == null)
            throw new IllegalArgumentException("invalid undead flag probe");
        return actor.channel().inbound().mobFlags(spawn.entityId());
    }

    public static WorldlineEvidence evidence(String signal, String signature) {
        return WorldlineEvidence.of(WorldlineHostileBehaviors.UNDEAD_SUN_BURN, WorldlineEvidence.VANILLA,
                signal, signature);
    }
}
