package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.BlockState;
import worldline.api.BlockPosition;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteChunkUnload;

/** Adapter-private Packet50 lifecycle state for a bounded decoded chunk set. */
final class B173RemoteWorldCache {
    private final Map<Long, RemoteChunkSnapshot> chunks = new LinkedHashMap<>();
    private final Deque<RemoteChunkUnload> unloads = new ArrayDeque<>();
    private int changes;
    private boolean implicitLoads;

    void preChunk(DataInputStream input) throws IOException {
        int x = input.readInt(), z = input.readInt(); boolean load = input.readBoolean();
        long key = key(x, z);
        if (!load) {
            boolean tracked = chunks.containsKey(key);
            chunks.remove(key);
            if (tracked) {
                if (unloads.size() >= RemoteWorldView.MAX_CHUNKS)
                    throw new IOException("remote chunk unload queue limit exceeded");
                unloads.addLast(new RemoteChunkUnload(x, z, chunks.size()));
            }
            return;
        }
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
        if (!chunks.containsKey(key)) {
            if (!implicitLoads) throw new IOException("chunk data arrived before prechunk load: "
                    + Math.floorDiv(region.x(), 16) + ":" + Math.floorDiv(region.z(), 16));
            if (chunks.size() >= RemoteWorldView.MAX_CHUNKS)
                throw new IOException("remote chunk cache limit exceeded");
        }
        chunks.put(key, snapshot); return true;
    }

    void enableImplicitLoads() { implicitLoads = true; }
    void reset() { chunks.clear(); unloads.clear(); changes = 0; }

    RemoteChunkUnload takeUnload(int chunkX, int chunkZ) {
        for (Iterator<RemoteChunkUnload> values = unloads.iterator(); values.hasNext();) {
            RemoteChunkUnload value = values.next();
            if (value.chunkX() == chunkX && value.chunkZ() == chunkZ) {
                values.remove();
                return value;
            }
        }
        return null;
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

    int changes() { return changes; }

    boolean matches(BlockPosition position, BlockState expected) {
        long key = key(Math.floorDiv(position.x(), 16), Math.floorDiv(position.z(), 16));
        RemoteChunkSnapshot chunk = chunks.get(key);
        return chunk != null && position.y() >= 0 && position.y() < 128
                && chunk.blockAt(Math.floorMod(position.x(), 16), position.y(),
                        Math.floorMod(position.z(), 16)).equals(expected);
    }

    void singleBlock(DataInputStream input) throws IOException {
        int x = input.readInt(), y = input.readUnsignedByte(), z = input.readInt();
        apply(x, y, z, input.readUnsignedByte(), input.readUnsignedByte());
    }

    void multiBlock(DataInputStream input) throws IOException {
        int chunkX = input.readInt(), chunkZ = input.readInt(), count = input.readUnsignedShort();
        short[] coordinates = new short[count];
        for (int index = 0; index < count; index++) coordinates[index] = input.readShort();
        byte[] ids = new byte[count], metadata = new byte[count];
        input.readFully(ids); input.readFully(metadata);
        for (int index = 0; index < count; index++) {
            int packed = coordinates[index] & 65535;
            apply(chunkX * 16 + (packed >> 12 & 15), packed & 255,
                    chunkZ * 16 + (packed >> 8 & 15), ids[index] & 255, metadata[index] & 15);
        }
    }

    RemoteExplosion explosion(DataInputStream input) throws IOException {
        double x = input.readDouble(), y = input.readDouble(), z = input.readDouble(); float strength = input.readFloat();
        int count = input.readInt(); if (count < 0 || count > RemoteExplosion.MAX_BLOCKS) throw new IOException("invalid explosion block count");
        int baseX = (int) Math.floor(x), baseY = (int) Math.floor(y), baseZ = (int) Math.floor(z);
        ArrayList<BlockPosition> destroyed = new ArrayList<>(count);
        for (int index = 0; index < count; index++) { BlockPosition position = new BlockPosition(baseX + input.readByte(), baseY + input.readByte(), baseZ + input.readByte());
            destroyed.add(position); apply(position.x(), position.y(), position.z(), 0, 0); }
        return new RemoteExplosion(x, y, z, strength, destroyed);
    }

    private void apply(int x, int y, int z, int id, int metadata) {
        if (y < 0 || y >= 128) return;
        long key = key(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        RemoteChunkSnapshot chunk = chunks.get(key); if (chunk == null) return;
        int localX = Math.floorMod(x, 16), localZ = Math.floorMod(z, 16);
        BlockState next = new BlockState(id, metadata);
        if (chunk.blockAt(localX, y, localZ).equals(next)) return;
        chunks.put(key, chunk.withBlock(localX, y, localZ, next)); changes++;
    }

    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }
}
