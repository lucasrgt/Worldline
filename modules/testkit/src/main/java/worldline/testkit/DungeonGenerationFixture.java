package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

/** Reusable fixed-region spawner, linked-chest, and accessible-loot observation. */
public final class DungeonGenerationFixture {
    private static final int SPAWNER = 52, CHEST = 54;
    private DungeonGenerationFixture() { }

    public static Evidence observe(RemoteWorldView world, int minX, int maxX, int minZ, int maxZ) {
        if (world == null || minX > maxX || minZ > maxZ
                || (long) (maxX - minX + 1) * (maxZ - minZ + 1) > RemoteWorldView.MAX_CHUNKS)
            throw new IllegalArgumentException("invalid dungeon census region");
        List<BlockPosition> spawners = blocks(world, minX, maxX, minZ, maxZ, SPAWNER);
        List<BlockPosition> chests = blocks(world, minX, maxX, minZ, maxZ, CHEST);
        List<BlockPosition> linked = new ArrayList<>(); BlockPosition selected = null, standing = null;
        for (BlockPosition chest : chests) {
            if (!nearSpawner(chest, spawners)) continue;
            linked.add(chest); BlockPosition candidate = standing(world, chest);
            if (selected == null && candidate != null) { selected = chest; standing = candidate; }
        }
        if (spawners.isEmpty() || linked.isEmpty() || selected == null)
            throw new IllegalStateException("fixed region lacks an accessible spawner-linked chest");
        int chunks = (maxX - minX + 1) * (maxZ - minZ + 1);
        return new Evidence(chunks, spawners, linked, selected, standing, digest(spawners, linked));
    }

    private static List<BlockPosition> blocks(RemoteWorldView world, int minX, int maxX,
            int minZ, int maxZ, int id) {
        List<BlockPosition> result = new ArrayList<>();
        for (int cx = minX; cx <= maxX; cx++) for (int cz = minZ; cz <= maxZ; cz++) {
            RemoteChunkSnapshot chunk = world.chunkAt(cx, cz);
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++)
                for (int y = 0; y < 128; y++) if (chunk.blockAt(x, y, z).legacyId() == id)
                    result.add(new BlockPosition(cx * 16 + x, y, cz * 16 + z));
        }
        result.sort(Comparator.comparingInt(BlockPosition::x).thenComparingInt(BlockPosition::y)
                .thenComparingInt(BlockPosition::z));
        return List.copyOf(result);
    }

    private static boolean nearSpawner(BlockPosition chest, List<BlockPosition> spawners) {
        return spawners.stream().anyMatch(value -> Math.abs(value.x() - chest.x()) <= 4
                && Math.abs(value.y() - chest.y()) <= 1 && Math.abs(value.z() - chest.z()) <= 4);
    }

    private static BlockPosition standing(RemoteWorldView world, BlockPosition chest) {
        for (int[] offset : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            BlockPosition value = new BlockPosition(chest.x() + offset[0], chest.y(), chest.z() + offset[1]);
            int cx = Math.floorDiv(value.x(), 16), cz = Math.floorDiv(value.z(), 16);
            if (value.y() > 0 && value.y() < 127 && world.containsChunk(cx, cz)
                    && world.blockAt(value.x(), value.y(), value.z()).legacyId() == 0
                    && world.blockAt(value.x(), value.y() + 1, value.z()).legacyId() == 0) return value;
        }
        return null;
    }

    private static String digest(List<BlockPosition> spawners, List<BlockPosition> chests) {
        try {
            MessageDigest value = MessageDigest.getInstance("SHA-256");
            for (BlockPosition position : spawners) update(value, "S", position);
            for (BlockPosition position : chests) update(value, "C", position);
            return HexFormat.of().formatHex(value.digest());
        } catch (Exception error) { throw new IllegalStateException("dungeon digest unavailable", error); }
    }

    private static void update(MessageDigest digest, String type, BlockPosition position) {
        digest.update((type + ':' + position.x() + ':' + position.y() + ':' + position.z() + '\n')
                .getBytes(StandardCharsets.UTF_8));
    }

    public static final class Evidence {
        private final int chunks;
        private final List<BlockPosition> spawners, linkedChests;
        private final BlockPosition selectedChest, standingPosition;
        private final String digest;

        Evidence(int chunks, List<BlockPosition> spawners, List<BlockPosition> linkedChests,
                BlockPosition selectedChest, BlockPosition standingPosition, String digest) {
            this.spawners = List.copyOf(spawners); this.linkedChests = List.copyOf(linkedChests);
            if (chunks < 1 || this.spawners.isEmpty() || this.linkedChests.isEmpty()
                    || selectedChest == null || standingPosition == null || digest == null
                    || !digest.matches("[0-9a-f]{64}"))
                throw new IllegalArgumentException("invalid dungeon generation evidence");
            this.chunks = chunks; this.selectedChest = selectedChest;
            this.standingPosition = standingPosition; this.digest = digest;
        }

        public int chunks() { return chunks; }
        public List<BlockPosition> spawners() { return spawners; }
        public List<BlockPosition> linkedChests() { return linkedChests; }
        public BlockPosition selectedChest() { return selectedChest; }
        public BlockPosition standingPosition() { return standingPosition; }
        public String digest() { return digest; }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return chunks == value.chunks && spawners.equals(value.spawners)
                    && linkedChests.equals(value.linkedChests)
                    && selectedChest.equals(value.selectedChest)
                    && standingPosition.equals(value.standingPosition) && digest.equals(value.digest);
        }
        @Override public int hashCode() {
            return Objects.hash(chunks, spawners, linkedChests, selectedChest, standingPosition, digest);
        }
    }
}
