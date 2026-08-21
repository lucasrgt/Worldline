package worldline.api;

/** Playable multiplayer session with bounded native chat send/receive. */
public interface ChatMultiplayerSession extends PlayableMultiplayerSession {
    void sendChat(String message);

    String awaitChat();
}
