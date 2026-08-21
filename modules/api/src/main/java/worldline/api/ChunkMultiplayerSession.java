package worldline.api;

/** Chat session that can consume one bounded remote chunk observation. */
public interface ChunkMultiplayerSession extends ChatMultiplayerSession {
    RemoteChunkObservation awaitChunk();
}
