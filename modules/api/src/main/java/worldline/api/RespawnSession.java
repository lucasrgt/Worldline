package worldline.api;

/** Typed dimension state extended with local health and one death respawn request. */
public interface RespawnSession extends DimensionSession {
    int health();
    int awaitHealth(int expected);
    RemoteRespawn respawn();
}
