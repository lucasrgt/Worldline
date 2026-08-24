package worldline.api;

import java.util.Objects;

/** Immutable protocol-14 Packet50 unload observation for one tracked chunk. */
public final class RemoteChunkUnload {
    private final int chunkX, chunkZ, remainingTrackedChunks;

    public RemoteChunkUnload(int chunkX, int chunkZ, int remainingTrackedChunks) {
        if (remainingTrackedChunks < 0 || remainingTrackedChunks > RemoteWorldView.MAX_CHUNKS)
            throw new IllegalArgumentException("invalid remaining chunk count");
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.remainingTrackedChunks = remainingTrackedChunks;
    }

    public int chunkX() { return chunkX; }
    public int chunkZ() { return chunkZ; }
    public int remainingTrackedChunks() { return remainingTrackedChunks; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteChunkUnload)) return false;
        RemoteChunkUnload value = (RemoteChunkUnload) other;
        return chunkX == value.chunkX && chunkZ == value.chunkZ
                && remainingTrackedChunks == value.remainingTrackedChunks;
    }
    @Override public int hashCode() {
        return Objects.hash(chunkX, chunkZ, remainingTrackedChunks);
    }
    @Override public String toString() {
        return "RemoteChunkUnload[chunk=" + chunkX + ":" + chunkZ
                + ",remaining=" + remainingTrackedChunks + "]";
    }
}
