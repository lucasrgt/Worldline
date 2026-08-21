package worldline.api;

/** One caller-controlled multiplayer protocol session. */
public interface MultiplayerSession extends AutoCloseable {
    void connect();

    MultiplayerState state();

    @Override
    void close();
}
