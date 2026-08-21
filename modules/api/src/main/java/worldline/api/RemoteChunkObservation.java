package worldline.api;

import java.util.Objects;

/** Immutable bounded observation of one remote chunk-region payload. */
public final class RemoteChunkObservation {
    private final int x, y, z, width, height, depth, payloadBytes;

    public RemoteChunkObservation(int x, int y, int z, int width, int height, int depth,
            int payloadBytes) {
        if (width < 1 || width > 256 || height < 1 || height > 256
                || depth < 1 || depth > 256) throw new IllegalArgumentException("invalid chunk dimensions");
        if (payloadBytes < 1 || payloadBytes > 4_000_000)
            throw new IllegalArgumentException("invalid chunk payload size");
        this.x = x; this.y = y; this.z = z;
        this.width = width; this.height = height; this.depth = depth;
        this.payloadBytes = payloadBytes;
    }

    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }
    public int width() { return width; }
    public int height() { return height; }
    public int depth() { return depth; }
    public int payloadBytes() { return payloadBytes; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RemoteChunkObservation)) return false;
        RemoteChunkObservation value = (RemoteChunkObservation) other;
        return x == value.x && y == value.y && z == value.z && width == value.width
                && height == value.height && depth == value.depth
                && payloadBytes == value.payloadBytes;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y, z, width, height, depth, payloadBytes); }
}
