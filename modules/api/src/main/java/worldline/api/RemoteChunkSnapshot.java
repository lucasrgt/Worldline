package worldline.api;

import java.util.Arrays;

/** Immutable coordinate-addressable contents of one bounded remote chunk region. */
public final class RemoteChunkSnapshot {
    private final RemoteChunkObservation observation;
    private final byte[] blockIds, metadata, blockLight, skyLight;

    public RemoteChunkSnapshot(RemoteChunkObservation observation, byte[] blockIds,
            byte[] metadata, byte[] blockLight, byte[] skyLight) {
        if (observation == null || blockIds == null || metadata == null
                || blockLight == null || skyLight == null)
            throw new IllegalArgumentException("null chunk snapshot field");
        int volume = volume(observation);
        if ((volume & 1) != 0 || blockIds.length != volume
                || metadata.length != volume / 2 || blockLight.length != volume / 2
                || skyLight.length != volume / 2)
            throw new IllegalArgumentException("chunk plane size mismatch");
        this.observation = observation;
        this.blockIds = blockIds.clone();
        this.metadata = metadata.clone();
        this.blockLight = blockLight.clone();
        this.skyLight = skyLight.clone();
    }

    public RemoteChunkObservation observation() { return observation; }

    public BlockState blockAt(int localX, int localY, int localZ) {
        int index = index(localX, localY, localZ);
        return new BlockState(blockIds[index] & 255, nibble(metadata, index));
    }

    public int blockLightAt(int localX, int localY, int localZ) {
        return nibble(blockLight, index(localX, localY, localZ));
    }

    public int skyLightAt(int localX, int localY, int localZ) {
        return nibble(skyLight, index(localX, localY, localZ));
    }

    public int blockCount() { return blockIds.length; }

    public int nonAirBlocks() {
        int count = 0;
        for (byte id : blockIds) if (id != 0) count++;
        return count;
    }

    private int index(int x, int y, int z) {
        if (x < 0 || x >= observation.width() || y < 0 || y >= observation.height()
                || z < 0 || z >= observation.depth())
            throw new IllegalArgumentException("chunk-local coordinate outside snapshot");
        return (x * observation.depth() + z) * observation.height() + y;
    }

    private static int nibble(byte[] values, int index) {
        int pair = values[index >> 1] & 255;
        return (index & 1) == 0 ? pair & 15 : pair >> 4;
    }

    private static int volume(RemoteChunkObservation value) {
        try { return Math.multiplyExact(Math.multiplyExact(value.width(), value.height()), value.depth()); }
        catch (ArithmeticException error) { throw new IllegalArgumentException("chunk volume overflow", error); }
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteChunkSnapshot)) return false;
        RemoteChunkSnapshot value = (RemoteChunkSnapshot) other;
        return observation.equals(value.observation) && Arrays.equals(blockIds, value.blockIds)
                && Arrays.equals(metadata, value.metadata)
                && Arrays.equals(blockLight, value.blockLight)
                && Arrays.equals(skyLight, value.skyLight);
    }

    @Override public int hashCode() {
        int result = observation.hashCode();
        result = 31 * result + Arrays.hashCode(blockIds);
        result = 31 * result + Arrays.hashCode(metadata);
        result = 31 * result + Arrays.hashCode(blockLight);
        return 31 * result + Arrays.hashCode(skyLight);
    }
}
