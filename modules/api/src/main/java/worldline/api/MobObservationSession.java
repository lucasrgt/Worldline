package worldline.api;

/** Explosion-capable session extended with one bounded server-authored mob-spawn observation. */
public interface MobObservationSession extends ExplosionSession {
    RemoteMobSpawn awaitMobSpawn(int legacyType);
    RemoteMobMovement awaitMobMovement(int entityId);
    void attackMob(int entityId);
    RemoteMobDeath awaitMobDeath(int entityId);
    RemoteMobMovement awaitObservedMobMovement();
    void attackObservedMob();
    RemoteMobDeath awaitObservedMobDeath();
    RemoteDroppedItem peekDroppedItem(RemoteItemStack expected);
}
