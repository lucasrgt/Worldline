package worldline.api;

/** Mob-capable session extended with one bounded Packet23 object-spawn observation. */
public interface ObjectObservationSession extends MobObservationSession {
    RemoteObjectSpawn awaitObjectSpawn(int type);
    void useSelectedItemInAir();
}
