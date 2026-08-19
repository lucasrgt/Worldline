package worldline.api;

/** Explosion-capable session extended with one bounded server-authored mob-spawn observation. */
public interface MobObservationSession extends ExplosionSession {
    RemoteMobSpawn awaitMobSpawn(int legacyType);
    RemoteMobMovement awaitMobMovement(int entityId);
}
