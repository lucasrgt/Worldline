package worldline.api;

/** Optional controlled capability for explicit chunk lifecycle tests. */
public interface ChunkLifecycleRuntime extends AutomatedMinecraftRuntime {
    void loadChunk(int chunkX, int chunkZ);

    void unloadChunk(int chunkX, int chunkZ);
}
