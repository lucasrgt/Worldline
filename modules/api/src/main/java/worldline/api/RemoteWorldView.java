package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable bounded view of decoded full remote chunks. */
public final class RemoteWorldView {
    public static final int MAX_CHUNKS = 256;
    private final List<RemoteChunkSnapshot> chunks;
    private final Map<Long, RemoteChunkSnapshot> byCoordinate;

    public RemoteWorldView(List<RemoteChunkSnapshot> values) {
        if (values == null || values.size() > MAX_CHUNKS)
            throw new IllegalArgumentException("invalid remote chunk collection");
        List<RemoteChunkSnapshot> copy = new ArrayList<>(values);
        copy.sort(Comparator.comparingInt(RemoteWorldView::chunkX)
                .thenComparingInt(RemoteWorldView::chunkZ));
        Map<Long, RemoteChunkSnapshot> indexed = new LinkedHashMap<>();
        for (RemoteChunkSnapshot chunk : copy) {
            validate(chunk); long key = key(chunkX(chunk), chunkZ(chunk));
            if (indexed.put(key, chunk) != null)
                throw new IllegalArgumentException("duplicate remote chunk coordinate");
        }
        chunks = Collections.unmodifiableList(copy);
        byCoordinate = Collections.unmodifiableMap(indexed);
    }

    public int loadedChunks() { return chunks.size(); }
    public List<RemoteChunkSnapshot> chunks() { return chunks; }
    public boolean containsChunk(int chunkX, int chunkZ) {
        return byCoordinate.containsKey(key(chunkX, chunkZ));
    }

    public RemoteChunkSnapshot chunkAt(int chunkX, int chunkZ) {
        RemoteChunkSnapshot chunk = byCoordinate.get(key(chunkX, chunkZ));
        if (chunk == null) throw new IllegalArgumentException("remote chunk is not loaded");
        return chunk;
    }

    public BlockState blockAt(int worldX, int worldY, int worldZ) {
        int chunkX = Math.floorDiv(worldX, 16), chunkZ = Math.floorDiv(worldZ, 16);
        RemoteChunkSnapshot chunk = chunkAt(chunkX, chunkZ);
        int localY = worldY - chunk.observation().y();
        return chunk.blockAt(Math.floorMod(worldX, 16), localY, Math.floorMod(worldZ, 16));
    }

    private static void validate(RemoteChunkSnapshot chunk) {
        if (chunk == null) throw new IllegalArgumentException("null remote chunk");
        RemoteChunkObservation region = chunk.observation();
        if (region.width() != 16 || region.height() != 128 || region.depth() != 16
                || region.y() != 0 || Math.floorMod(region.x(), 16) != 0
                || Math.floorMod(region.z(), 16) != 0)
            throw new IllegalArgumentException("remote world requires aligned full chunks");
    }

    private static int chunkX(RemoteChunkSnapshot value) { return Math.floorDiv(value.observation().x(), 16); }
    private static int chunkZ(RemoteChunkSnapshot value) { return Math.floorDiv(value.observation().z(), 16); }
    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }

    @Override public boolean equals(Object other) {
        return other instanceof RemoteWorldView && chunks.equals(((RemoteWorldView) other).chunks);
    }
    @Override public int hashCode() { return chunks.hashCode(); }
}
