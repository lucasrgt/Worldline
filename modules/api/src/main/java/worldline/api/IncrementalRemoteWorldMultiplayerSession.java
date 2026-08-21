package worldline.api;

/** Cached session with server-authoritative block-break intent and updates. */
public interface IncrementalRemoteWorldMultiplayerSession extends CachedRemoteWorldMultiplayerSession {
    void beginBreak(BlockPosition position);
    void finishBreak(BlockPosition position);
    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected);
}
