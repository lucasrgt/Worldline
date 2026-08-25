package worldline.testkit;

import java.util.Objects;
import java.util.Random;
import worldline.api.RemoteMobSpawn;

/** Reusable Beta slime-chunk membership and natural Packet24 observation contract. */
public final class NaturalSlimeSpawnFixture {
    private NaturalSlimeSpawnFixture() { }

    public static Evidence await(long seed, int minX, int maxX, int minZ, int maxZ,
            int maximumAttempts, Attempt attempt) {
        int qualifying = qualifyingChunks(seed, minX, maxX, minZ, maxZ);
        if (qualifying < 1 || maximumAttempts < 1 || attempt == null)
            throw new IllegalArgumentException("invalid natural slime attempt boundary");
        for (int index = 1; index <= maximumAttempts; index++) {
            RemoteMobSpawn spawn = attempt.observe(index);
            if (spawn != null && spawn.legacyType() == 55 && spawn.y() < 16D
                    && inside(chunk(spawn.x()), minX, maxX)
                    && inside(chunk(spawn.z()), minZ, maxZ)
                    && slimeChunk(seed, chunk(spawn.x()), chunk(spawn.z())))
                return new Evidence(seed, minX, maxX, minZ, maxZ, qualifying, maximumAttempts);
        }
        throw new IllegalStateException("natural slime absent after bounded attempts");
    }

    public static boolean slimeChunk(long seed, int chunkX, int chunkZ) {
        Random random = new Random(seed + (long) (chunkX * chunkX * 0x4c1906)
                + (long) (chunkX * 0x5ac0db) + (long) (chunkZ * chunkZ) * 0x4307a7L
                + (long) (chunkZ * 0x5f24f) ^ 987234911L);
        return random.nextInt(10) == 0;
    }

    public static int qualifyingChunks(long seed, int minX, int maxX, int minZ, int maxZ) {
        if (minX > maxX || minZ > maxZ || (long) (maxX - minX + 1) * (maxZ - minZ + 1) > 81)
            throw new IllegalArgumentException("invalid slime chunk matrix");
        int result = 0;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++)
            if (slimeChunk(seed, x, z)) result++;
        return result;
    }

    private static int chunk(double coordinate) {
        return Math.floorDiv((int) Math.floor(coordinate), 16);
    }
    private static boolean inside(int value, int minimum, int maximum) {
        return value >= minimum && value <= maximum;
    }

    @FunctionalInterface public interface Attempt {
        RemoteMobSpawn observe(int attempt);
    }

    public static final class Evidence {
        private final long seed; private final int minX, maxX, minZ, maxZ;
        private final int qualifyingChunks, maximumAttempts;
        Evidence(long seed, int minX, int maxX, int minZ, int maxZ,
                int qualifyingChunks, int maximumAttempts) {
            this.seed = seed; this.minX = minX; this.maxX = maxX;
            this.minZ = minZ; this.maxZ = maxZ; this.qualifyingChunks = qualifyingChunks;
            this.maximumAttempts = maximumAttempts;
        }
        public long seed() { return seed; }
        public int minimumChunkX() { return minX; }
        public int maximumChunkX() { return maxX; }
        public int minimumChunkZ() { return minZ; }
        public int maximumChunkZ() { return maxZ; }
        public int qualifyingChunks() { return qualifyingChunks; }
        public int maximumAttempts() { return maximumAttempts; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return seed == value.seed && minX == value.minX && maxX == value.maxX
                    && minZ == value.minZ && maxZ == value.maxZ
                    && qualifyingChunks == value.qualifyingChunks
                    && maximumAttempts == value.maximumAttempts;
        }
        @Override public int hashCode() { return Objects.hash(seed, minX, maxX, minZ, maxZ,
                qualifyingChunks, maximumAttempts); }
    }
}
