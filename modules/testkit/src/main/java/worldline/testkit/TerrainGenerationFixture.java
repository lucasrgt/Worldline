package worldline.testkit;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Set;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

/** Reusable bounded geology, surface, cave-air, and ore-component census. */
public final class TerrainGenerationFixture {
    private TerrainGenerationFixture() { }

    public static Evidence observe(RemoteWorldView world, int minX, int maxX,
            int minZ, int maxZ) {
        validate(world, minX, maxX, minZ, maxZ);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            Set<String> surfaces = new HashSet<String>();
            int chunks = 0, caveAir = 0, oreBlocks = 0, oreComponents = 0;
            for (int chunkX = minX; chunkX <= maxX; chunkX++) {
                for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                    RemoteChunkSnapshot chunk = world.chunkAt(chunkX, chunkZ);
                    chunks++;
                    digest.update(ByteBuffer.allocate(8).putInt(chunkX).putInt(chunkZ).array());
                    boolean[] ore = new boolean[32768];
                    for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
                        int top = top(chunk, x, z);
                        surfaces.add(family(chunk.blockAt(x, top, z).legacyId()) + ':' + top);
                        for (int y = 0; y < 128; y++) {
                            int id = chunk.blockAt(x, y, z).legacyId();
                            digest.update((byte) (geology(id) ? id : 0));
                            if (y >= 5 && y + 5 < top && id == 0) caveAir++;
                            if (ore(id)) {
                                ore[index(x, y, z)] = true;
                                oreBlocks++;
                            }
                        }
                    }
                    oreComponents += components(ore);
                }
            }
            return new Evidence(HexFormat.of().formatHex(digest.digest()), chunks,
                    surfaces.size(), caveAir, oreBlocks, oreComponents);
        } catch (Exception error) {
            throw new IllegalStateException("terrain generation census unavailable", error);
        }
    }

    private static void validate(RemoteWorldView world, int minX, int maxX,
            int minZ, int maxZ) {
        long chunks = (long) (maxX - minX + 1) * (maxZ - minZ + 1);
        if (world == null || minX > maxX || minZ > maxZ
                || chunks < 1 || chunks > RemoteWorldView.MAX_CHUNKS) {
            throw new IllegalArgumentException("invalid terrain census region");
        }
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (!world.containsChunk(x, z)) {
                throw new IllegalArgumentException("terrain census chunk is absent");
            }
        }
    }

    private static int components(boolean[] ore) {
        boolean[] seen = new boolean[ore.length];
        int[] queue = new int[ore.length];
        int count = 0;
        for (int start = 0; start < ore.length; start++) {
            if (!ore[start] || seen[start]) continue;
            count++;
            int read = 0, write = 0;
            queue[write++] = start;
            seen[start] = true;
            while (read < write) {
                int at = queue[read++], y = at & 127, cell = at >> 7;
                int z = cell & 15, x = cell >> 4;
                write = add(ore, seen, queue, write, x - 1, y, z);
                write = add(ore, seen, queue, write, x + 1, y, z);
                write = add(ore, seen, queue, write, x, y - 1, z);
                write = add(ore, seen, queue, write, x, y + 1, z);
                write = add(ore, seen, queue, write, x, y, z - 1);
                write = add(ore, seen, queue, write, x, y, z + 1);
            }
        }
        return count;
    }

    private static int add(boolean[] ore, boolean[] seen, int[] queue, int write,
            int x, int y, int z) {
        if (x < 0 || x > 15 || y < 0 || y > 127 || z < 0 || z > 15) return write;
        int at = index(x, y, z);
        if (ore[at] && !seen[at]) {
            seen[at] = true;
            queue[write++] = at;
        }
        return write;
    }

    private static int top(RemoteChunkSnapshot chunk, int x, int z) {
        for (int y = 127; y >= 0; y--) {
            if (surface(chunk.blockAt(x, y, z).legacyId())) return y;
        }
        throw new IllegalStateException("empty worldgen column");
    }

    private static boolean surface(int id) {
        return id != 0 && id != 6 && id != 17 && id != 18 && id != 31 && id != 32
                && id != 37 && id != 38 && id != 39 && id != 40;
    }

    private static String family(int id) {
        if (id == 8 || id == 9) return "water";
        if (id == 12 || id == 13 || id == 24) return "sand";
        if (id == 78 || id == 79 || id == 80) return "snow";
        if (id == 2 || id == 3) return "grass";
        return "other";
    }

    private static boolean ore(int id) {
        return id == 14 || id == 15 || id == 16 || id == 21
                || id == 56 || id == 73 || id == 74;
    }

    private static boolean geology(int id) { return id == 1 || id == 7 || ore(id); }
    private static int index(int x, int y, int z) { return (x * 16 + z) * 128 + y; }

    /** Equatable normalized evidence, intentionally excluding mutable decoration. */
    public static final class Evidence {
        private final String geology;
        private final int chunks, surfaceFamilies, caveAir, oreBlocks, oreComponents;

        Evidence(String geology, int chunks, int surfaceFamilies, int caveAir,
                int oreBlocks, int oreComponents) {
            if (geology == null || !geology.matches("[0-9a-f]{64}") || chunks < 1
                    || surfaceFamilies < 1 || caveAir < 0 || oreBlocks < 0
                    || oreComponents < 0 || oreComponents > oreBlocks) {
                throw new IllegalArgumentException("invalid terrain generation evidence");
            }
            this.geology = geology;
            this.chunks = chunks;
            this.surfaceFamilies = surfaceFamilies;
            this.caveAir = caveAir;
            this.oreBlocks = oreBlocks;
            this.oreComponents = oreComponents;
        }

        public String geology() { return geology; }
        public int chunks() { return chunks; }
        public int surfaceFamilies() { return surfaceFamilies; }
        public int caveAir() { return caveAir; }
        public int oreBlocks() { return oreBlocks; }
        public int oreComponents() { return oreComponents; }

        public boolean replayEquals(Evidence value) {
            return value != null && geology.equals(value.geology) && chunks == value.chunks
                    && oreBlocks == value.oreBlocks && oreComponents == value.oreComponents;
        }

        public String describe() {
            return "chunks=" + chunks + ",geology=" + geology
                    + ",surfaceFamilies=" + surfaceFamilies + ",caveAir=" + caveAir
                    + ",oreBlocks=" + oreBlocks + ",oreVeins=" + oreComponents;
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return geology.equals(value.geology) && chunks == value.chunks
                    && surfaceFamilies == value.surfaceFamilies && caveAir == value.caveAir
                    && oreBlocks == value.oreBlocks && oreComponents == value.oreComponents;
        }

        @Override public int hashCode() {
            return Objects.hash(geology, chunks, surfaceFamilies, caveAir,
                    oreBlocks, oreComponents);
        }
    }
}
