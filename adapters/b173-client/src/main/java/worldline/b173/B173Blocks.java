package worldline.b173;

import java.util.Set;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.World;
import worldline.api.ItemCensus;

/** Counts loaded non-air block IDs in a chunk window around the player. */
final class B173Blocks {
    private static final int RADIUS = 4;

    private B173Blocks() {}

    static ItemCensus census(World world, int chunkX, int chunkZ) {
        int[] counts = new int[256];
        IChunkProvider provider = world.getIChunkProvider();
        for (int x = chunkX - RADIUS; x <= chunkX + RADIUS; x++) {
            for (int z = chunkZ - RADIUS; z <= chunkZ + RADIUS; z++) {
                if (!provider.chunkExists(x, z)) continue;
                Chunk chunk = world.getChunkFromChunkCoords(x, z);
                if (chunk.blocks == null) continue;
                for (int index = 0; index < chunk.blocks.length; index++) {
                    int id = chunk.blocks[index] & 255;
                    if (id != 0) counts[id]++;
                }
            }
        }
        ItemCensus census = ItemCensus.empty();
        for (int id = 1; id < counts.length; id++) {
            if (counts[id] > 0) census = census.plus(id, counts[id]);
        }
        return census;
    }

    static ItemCensus inChunks(World world, Set<Long> chunks) {
        if (world == null || chunks == null) throw new NullPointerException("chunks");
        if (chunks.isEmpty()) return ItemCensus.empty();
        int[] counts = new int[256];
        IChunkProvider provider = world.getIChunkProvider();
        for (Long packed : chunks) {
            long key = packed.longValue();
            int x = (int) (key >> 32);
            int z = (int) key;
            if (!provider.chunkExists(x, z)) continue;
            Chunk chunk = world.getChunkFromChunkCoords(x, z);
            if (chunk.blocks == null) continue;
            for (int index = 0; index < chunk.blocks.length; index++) {
                int id = chunk.blocks[index] & 255;
                if (id != 0) counts[id]++;
            }
        }
        ItemCensus census = ItemCensus.empty();
        for (int id = 1; id < counts.length; id++) {
            if (counts[id] > 0) census = census.plus(id, counts[id]);
        }
        return census;
    }
}
