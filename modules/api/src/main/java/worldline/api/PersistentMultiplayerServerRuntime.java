package worldline.api;

/** Multiplayer server control extended with persisted player observation. */
public interface PersistentMultiplayerServerRuntime extends MultiplayerServerRuntime {
    ServerPlayerState player(String username);
}
