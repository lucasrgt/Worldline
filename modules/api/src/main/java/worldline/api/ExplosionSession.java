package worldline.api;

/** Respawn-capable multiplayer session extended with one server explosion observation. */
public interface ExplosionSession extends RespawnSession {
    RemoteExplosion awaitExplosion();
}
