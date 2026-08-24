package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkUnload;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteObjectSpawn;

/** Reusable evidence boundary for persistent state across one chunk unload and reload. */
public final class ChunkReloadFixture {
    private static final BlockState LIT_FURNACE = new BlockState(62, 0);
    private static final RemoteItemStack GLASS = new RemoteItemStack(20, 1, 0);
    private ChunkReloadFixture() { }

    public static Evidence observe(int chunkX, int chunkZ, BlockPosition furnace,
            BlockState burningBefore, BlockState burningAfter, RemoteItemStack furnaceOutput,
            RemoteDroppedItem itemBefore, RemoteDroppedItem itemAfter,
            RemoteObjectSpawn cartBefore, RemoteObjectSpawn cartAfter, RemoteChunkUnload unload) {
        if (furnace == null || burningBefore == null || burningAfter == null
                || furnaceOutput == null || itemBefore == null || itemAfter == null
                || cartBefore == null || cartAfter == null || unload == null)
            throw new IllegalArgumentException("null chunk reload evidence");
        require(cellChunk(furnace.x()) == chunkX && cellChunk(furnace.z()) == chunkZ,
                "furnace escaped target chunk");
        require(burningBefore.legacyId() == LIT_FURNACE.legacyId()
                && burningAfter.legacyId() == LIT_FURNACE.legacyId()
                && furnaceOutput.equals(GLASS), "burning furnace did not survive reload");
        require(unload.chunkX() == chunkX && unload.chunkZ() == chunkZ,
                "Packet50 did not unload target chunk");
        require(itemBefore.entityId() != itemAfter.entityId()
                && itemBefore.item().equals(itemAfter.item()) && inChunk(itemBefore, chunkX, chunkZ)
                && inChunk(itemAfter, chunkX, chunkZ),
                "dropped item reload drift: before=" + itemBefore + ",after=" + itemAfter);
        double cartDistance = distance(cartBefore, cartAfter);
        require(cartBefore.entityId() != cartAfter.entityId()
                && cartBefore.type() == 10 && cartAfter.type() == 10
                && inChunk(cartBefore, chunkX, chunkZ) && inChunk(cartAfter, chunkX, chunkZ)
                && cartDistance <= 9D, "minecart reload drift: ids=" + cartBefore.entityId()
                        + "->" + cartAfter.entityId() + ",positions=" + cartBefore.x() + ":"
                        + cartBefore.y() + ":" + cartBefore.z() + "->" + cartAfter.x() + ":"
                        + cartAfter.y() + ":" + cartAfter.z() + ",distanceSquared=" + cartDistance);
        return new Evidence(chunkX, chunkZ, furnace, furnaceOutput, itemAfter.item(), cartAfter.type());
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
        private final BlockPosition furnace;
        private final RemoteItemStack furnaceOutput, item;

        Evidence(int chunkX, int chunkZ, BlockPosition furnace, RemoteItemStack furnaceOutput,
                RemoteItemStack item, int cartType) {
            this.chunkX = chunkX; this.chunkZ = chunkZ; this.furnace = furnace;
            this.furnaceOutput = furnaceOutput; this.item = item; this.cartType = cartType;
        }
        public int chunkX() { return chunkX; }
        public int chunkZ() { return chunkZ; }
        public BlockPosition furnace() { return furnace; }
        public RemoteItemStack furnaceOutput() { return furnaceOutput; }
        public RemoteItemStack item() { return item; }
        public int cartType() { return cartType; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return chunkX == value.chunkX && chunkZ == value.chunkZ && cartType == value.cartType
                    && furnace.equals(value.furnace) && furnaceOutput.equals(value.furnaceOutput)
                    && item.equals(value.item);
        }
        @Override public int hashCode() {
            return Objects.hash(chunkX, chunkZ, furnace, furnaceOutput, item, cartType);
        }
    }
}
