package worldline.api;

/** Multiplayer session advanced through the initial play-position exchange. */
public interface PlayableMultiplayerSession extends MultiplayerSession {
    PlayerPose synchronizePose();

    void look(float yaw, float pitch);
}
