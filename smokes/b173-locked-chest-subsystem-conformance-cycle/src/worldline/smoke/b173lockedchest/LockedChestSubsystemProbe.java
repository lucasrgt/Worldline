package worldline.smoke.b173lockedchest;

import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/** Proves locked-chest placement, harvest, persistence, physics, and timed removal. */
final class LockedChestSubsystemProbe {
    final int placementRoute, placedState, stackAfter, metadataMask;
    final int strengthClass, breakBefore, breakAfter, dropDelta, dropId, dropCount, savedState;
    final int collision, lightCode, tickMask, tickBefore, tickAfter, neighborState;
    private LockedChestSubsystemProbe(int placementRoute, int placedState, int stackAfter,
            int metadataMask, int strengthClass, int breakBefore, int breakAfter, int dropDelta,
            int dropId, int dropCount, int savedState, int collision, int lightCode, int tickMask,
            int tickBefore, int tickAfter, int neighborState) {
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.metadataMask = metadataMask;
        this.strengthClass = strengthClass;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.dropId = dropId;
        this.dropCount = dropCount;
        this.savedState = savedState;
        this.collision = collision;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickBefore = tickBefore;
        this.tickAfter = tickAfter;
        this.neighborState = neighborState;
    }
    static LockedChestSubsystemProbe execute(World world) {
        EntityPlayer player = new EntityPlayer(world) {
        };
        require(world.setBlockAndMetadataWithNotify(20, 79, 20, 1, 0),
                "locked-chest placement support failed");
        ItemStack stack = new ItemStack(95, 1, 0);
        boolean placed = Item.itemsList[95].onItemUse(stack, player, world, 20, 79, 20, 1);
        int placedState = state(world, 20, 80, 20);

        require(world.setBlockAndMetadataWithNotify(24, 80, 20, 95, 0),
                "locked-chest break cell failed");
        int entities = world.loadedEntityList.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = Block.lockedChest.blockStrength(player);
        boolean removed = world.setBlockWithNotify(24, 80, 20, 0);
        if (removed && player.canHarvestBlock(Block.lockedChest))
            Block.lockedChest.harvestBlock(world, player, 24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);
        EntityItem drop = lastDrop(world, entities);

        require(world.setBlockAndMetadataWithNotify(28, 80, 20, 95, 0),
                "locked-chest persistence cell failed");
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(1, 1), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int saved = loaded.getBlockID(12, 80, 4) * 100 + loaded.getBlockMetadata(12, 80, 4);

        AxisAlignedBB box = Block.lockedChest.getCollisionBoundingBoxFromPool(world, 20, 80, 20);
        int collision = box != null && box.minX == 20D && box.minY == 80D && box.minZ == 20D
                && box.maxX == 21D && box.maxY == 81D && box.maxZ == 21D ? 1 : 0;
        int light = Block.lightOpacity[95] * 100 + Block.lightValue[95];
        require(world.setBlockAndMetadataWithNotify(32, 80, 20, 95, 0),
                "locked-chest neighbor cell failed");
        Block.lockedChest.onNeighborBlockChange(world, 32, 80, 20, 1);
        Block.lockedChest.onNeighborBlockChange(world, 32, 80, 20, 69);
        int neighbor = state(world, 32, 80, 20);
        int tickBefore = state(world, 20, 80, 20);
        Block.lockedChest.updateTick(world, 20, 80, 20, new Random(17320110795L));
        int tickAfter = state(world, 20, 80, 20);
        LockedChestSubsystemProbe result = new LockedChestSubsystemProbe(placed ? 1 : 0,
                placedState, stack.stackSize, 1 << world.getBlockMetadata(20, 80, 20),
                Float.isInfinite(strength) ? 1 : 0, breakBefore, breakAfter,
                world.loadedEntityList.size() - entities, drop == null ? 0 : drop.item.itemID,
                drop == null ? 0 : drop.item.stackSize, saved, collision, light,
                Block.tickOnLoad[95] ? 1 : 0, tickBefore, tickAfter, neighbor);
        result.validate();
        return result;
    }
    String domains() {
        return "95=0,item-route=95x1->0,placed=95:0";
    }
    String lifecycle() {
        return "break=95:0->0:0,strength=infinite,drop=95x1";
    }
    String persistence() {
        return "chunk-nbt=95:0";
    }
    String physics() {
        return "collision=full,light=255:15";
    }
    String timing() {
        return "random-enrolled=T,callback=95:0->0:0";
    }
    String neighbors() {
        return "stone+lever=stable-95:0";
    }
    private void validate() {
        require(placementRoute == 1 && placedState == 9500 && stackAfter == 0
                && metadataMask == 1, "locked-chest item placement or domain drifted");
        require(strengthClass == 1 && breakBefore == 9500 && breakAfter == 0
                && dropDelta == 1 && dropId == 95 && dropCount == 1,
                "locked-chest harvest lifecycle drifted");
        require(savedState == 9500, "locked-chest chunk round trip drifted");
        require(collision == 1 && lightCode == 25515,
                "locked-chest physical envelope drifted");
        require(tickMask == 1 && tickBefore == 9500 && tickAfter == 0,
                "locked-chest tick removal drifted");
        require(neighborState == 9500, "locked-chest neighbor stability drifted");
    }
    private static EntityItem lastDrop(World world, int first) {
        for (int index = world.loadedEntityList.size() - 1; index >= first; index--) {
            Object entity = world.loadedEntityList.get(index);
            if (entity instanceof EntityItem)
                return (EntityItem) entity;
        }
        return null;
    }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
