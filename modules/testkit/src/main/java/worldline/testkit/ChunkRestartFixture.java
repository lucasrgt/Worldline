package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.RemoteChunkUnload;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteObjectSpawn;

/**
 * Reusable evidence boundary for semantic state across one full dedicated-server
 * restart. Unstable entity IDs are normalized out: they are neither compared nor
 * published, so pre-restart and post-restart observations stay equatable.
 */
public final class ChunkRestartFixture {
    private static final int MINECART = 10;
    private ChunkRestartFixture() { }

    public static Evidence await(int chunkX, int chunkZ, BlockPosition chest,
            RemoteItemStack stored, RemoteContainerWindow reopened,
            RemoteDroppedItem itemBefore, RemoteDroppedItem itemAfter,
            RemoteObjectSpawn cartBefore, RemoteObjectSpawn cartAfter,
            RemoteChunkUnload unload) {
        if (chest == null || stored == null || reopened == null || itemBefore == null
                || itemAfter == null || cartBefore == null || cartAfter == null
                || unload == null)
            throw new IllegalArgumentException("null chunk restart evidence");
        require(cellChunk(chest.x()) == chunkX && cellChunk(chest.z()) == chunkZ,
                "chest escaped target chunk");
        require(reopened.inventory().occupiedSlots() == 1
                && reopened.inventory().slot(0).item().equals(stored),
                "restarted chest inventory drifted");
        require(unload.chunkX() == chunkX && unload.chunkZ() == chunkZ,
                "Packet50 did not unload the target chunk before restart");
        require(itemBefore.item().equals(itemAfter.item())
                && inChunk(itemBefore, chunkX, chunkZ) && inChunk(itemAfter, chunkX, chunkZ),
                "dropped item restart drift: before=" + itemBefore + ",after=" + itemAfter);
        double distance = distance(cartBefore, cartAfter);
        require(cartBefore.type() == MINECART && cartAfter.type() == MINECART
                && inChunk(cartBefore, chunkX, chunkZ) && inChunk(cartAfter, chunkX, chunkZ)
                && distance <= 9D, "minecart restart drift: types="
                        + cartBefore.type() + ":" + cartAfter.type()
                        + ",positions=" + cartBefore.x() + ":" + cartBefore.y() + ":"
                        + cartBefore.z() + "->" + cartAfter.x() + ":" + cartAfter.y() + ":"
                        + cartAfter.z() + ",distanceSquared=" + distance);
        return new Evidence(chunkX, chunkZ, chest, stored, itemAfter.item(), cartAfter.type());
    }

    private static boolean inChunk(RemoteDroppedItem item, int x, int z) {
        return cellChunk(item.x()) == x && cellChunk(item.z()) == z;
    }
    private static boolean inChunk(RemoteObjectSpawn item, int x, int z) {
        return cellChunk(item.x()) == x && cellChunk(item.z()) == z;
    }
    private static int cellChunk(double value) {
        return Math.floorDiv((int) Math.floor(value), 16);
    }
    private static double distance(RemoteObjectSpawn first, RemoteObjectSpawn second) {
        return square(first.x() - second.x()) + square(first.y() - second.y())
                + square(first.z() - second.z());
    }
    private static double square(double value) { return value * value; }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final int chunkX, chunkZ, cartType;
        private final BlockPosition chest;
        private final RemoteItemStack stored, item;

        Evidence(int chunkX, int chunkZ, BlockPosition chest, RemoteItemStack stored,
                RemoteItemStack item, int cartType) {
            this.chunkX = chunkX; this.chunkZ = chunkZ; this.chest = chest;
            this.stored = stored; this.item = item; this.cartType = cartType;
        }
        public int chunkX() { return chunkX; }
        public int chunkZ() { return chunkZ; }
        public BlockPosition chest() { return chest; }
        public RemoteItemStack stored() { return stored; }
        public RemoteItemStack item() { return item; }
        public int cartType() { return cartType; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return chunkX == value.chunkX && chunkZ == value.chunkZ && cartType == value.cartType
                    && chest.equals(value.chest) && stored.equals(value.stored)
                    && item.equals(value.item);
        }
        @Override public int hashCode() {
            return Objects.hash(chunkX, chunkZ, chest, stored, item, cartType);
        }
    }
}
