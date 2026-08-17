package worldline.api;

/** Chunk session that can decode one bounded remote region into neutral block state. */
public interface RemoteWorldMultiplayerSession extends ChunkMultiplayerSession {
    RemoteChunkSnapshot awaitChunkSnapshot();
}
