package worldline.api;

/** One protocol-14 session with typed current-dimension observation. */
public interface DimensionSession extends PeerSwingSession {
    int dimension();
    int awaitDimension(int expected);
}
