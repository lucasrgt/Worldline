package worldline.b173;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import worldline.api.ItemCensus;

/** Loaded-chunk keys and the item totals that sit inside a chunk set. */
final class B173Chunks {
    private static final int ORIGIN_RADIUS = 8;
    private static final int PLAYER_RADIUS = 4;

    private B173Chunks() {}

    static Set<Long> loaded(World world) {
        if (world == null) throw new NullPointerException("world");
        Set<Long> keys = new HashSet<Long>();
        IChunkProvider provider = world.getIChunkProvider();
        addWindow(provider, keys, 0, 0, ORIGIN_RADIUS);
        for (Object value : world.playerEntities) {
            Entity player = (Entity) value;
            addWindow(provider, keys, chunkX(player), chunkZ(player), PLAYER_RADIUS);
        }
        return keys;
    }

    static ItemCensus items(World world, Set<Long> chunks) {
        if (world == null || chunks == null) throw new NullPointerException("chunks");
        ItemCensus census = ItemCensus.empty();
        if (chunks.isEmpty()) return census;
        for (Object value : world.loadedEntityList) {
            Entity entity = (Entity) value;
            if (!chunks.contains(key(chunkX(entity), chunkZ(entity)))) continue;
            if (entity instanceof EntityItem) {
                census = B173Items.add(census, ((EntityItem) entity).item);
            } else if (entity instanceof IInventory && !(entity instanceof EntityPlayer)) {
                census = B173Items.add(census, (IInventory) entity);
            }
        }
        for (Object value : world.loadedTileEntityList) {
            if (!(value instanceof IInventory)) continue;
            TileEntity tile = (TileEntity) value;
            if (chunks.contains(key(tile.xCoord >> 4, tile.zCoord >> 4))) {
                census = B173Items.add(census, (IInventory) tile);
            }
        }
        return census;
    }

    static long key(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | ((long) chunkZ & 0xffffffffL);
    }

    private static void addWindow(IChunkProvider provider, Set<Long> keys, int cx, int cz, int radius) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if (provider.chunkExists(x, z)) keys.add(key(x, z));
            }
        }
    }

    private static int chunkX(Entity entity) {
        return entity.addedToChunk ? entity.chunkCoordX : ((int) Math.floor(entity.posX)) >> 4;
    }

    private static int chunkZ(Entity entity) {
        return entity.addedToChunk ? entity.chunkCoordZ : ((int) Math.floor(entity.posZ)) >> 4;
    }
}
