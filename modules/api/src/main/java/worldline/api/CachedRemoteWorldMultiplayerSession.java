package worldline.api;

/** Remote-world session that pumps a bounded set of lifecycle-qualified chunks. */
public interface CachedRemoteWorldMultiplayerSession extends RemoteWorldMultiplayerSession {
    RemoteWorldView awaitRemoteWorld(int minimumChunks);
    RemoteWorldView awaitRemoteChunk(int chunkX, int chunkZ);
    RemoteChunkUnload awaitRemoteChunkUnload(int chunkX, int chunkZ);
}
