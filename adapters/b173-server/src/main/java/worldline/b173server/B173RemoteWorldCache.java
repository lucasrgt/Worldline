package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

/** Adapter-private Packet50 lifecycle state for a bounded decoded chunk set. */
final class B173RemoteWorldCache {
    private final Map<Long, RemoteChunkSnapshot> chunks = new LinkedHashMap<>();

    void preChunk(DataInputStream input) throws IOException {
        int x = input.readInt(), z = input.readInt(); boolean load = input.readBoolean();
        long key = key(x, z);
        if (!load) { chunks.remove(key); return; }
        if (!chunks.containsKey(key) && chunks.size() >= RemoteWorldView.MAX_CHUNKS)
            throw new IOException("remote chunk cache limit exceeded");
        if (!chunks.containsKey(key)) chunks.put(key, null);
    }

    boolean accept(RemoteChunkSnapshot snapshot) throws IOException {
        RemoteChunkObservation region = snapshot.observation();
        if (region.width() != 16 || region.height() != 128 || region.depth() != 16
                || region.y() != 0 || Math.floorMod(region.x(), 16) != 0
                || Math.floorMod(region.z(), 16) != 0) return false;
        long key = key(Math.floorDiv(region.x(), 16), Math.floorDiv(region.z(), 16));
        if (!chunks.containsKey(key)) throw new IOException("chunk data arrived before prechunk load");
        chunks.put(key, snapshot); return true;
    }

    int decoded() {
        int count = 0;
        for (RemoteChunkSnapshot snapshot : chunks.values()) if (snapshot != null) count++;
        return count;
    }

    int tracked() { return chunks.size(); }

    RemoteChunkSnapshot firstDecoded() {
        for (RemoteChunkSnapshot snapshot : chunks.values()) if (snapshot != null) return snapshot;
        return null;
    }

    RemoteWorldView snapshot() {
        ArrayList<RemoteChunkSnapshot> decoded = new ArrayList<>();
        for (RemoteChunkSnapshot snapshot : chunks.values()) if (snapshot != null) decoded.add(snapshot);
        return new RemoteWorldView(decoded);
    }

    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }
}
