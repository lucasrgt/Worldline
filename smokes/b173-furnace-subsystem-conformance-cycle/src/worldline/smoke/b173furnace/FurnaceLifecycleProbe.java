package worldline.smoke.b173furnace;

import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntityFurnace;
import net.minecraft.src.World;

/** Proves active lifecycle, content drops, chunk-NBT progress, collision, and light. */
final class FurnaceLifecycleProbe {
    final int breakAfter, dropCount, dropCode, savedState, savedProgress;
    final long savedSlots;
    final int idleCollision, activeCollision, lightCode;

    private FurnaceLifecycleProbe(int breakAfter, int dropCount, int dropCode, int savedState,
            int savedProgress, long savedSlots, int idleCollision, int activeCollision,
            int lightCode) {
        this.breakAfter = breakAfter;
        this.dropCount = dropCount;
        this.dropCode = dropCode;
        this.savedState = savedState;
        this.savedProgress = savedProgress;
        this.savedSlots = savedSlots;
        this.idleCollision = idleCollision;
        this.activeCollision = activeCollision;
        this.lightCode = lightCode;
    }

    static FurnaceLifecycleProbe execute(World world) {
        int savedX = 36;
        int y = 92;
        int savedZ = 36;
        place(world, savedX, y, savedZ, 62, 5);
        TileEntityFurnace saved = tile(world, savedX, y, savedZ);
        populate(saved);
        saved.furnaceBurnTime = 777;
        saved.currentItemBurnTime = 1600;
        saved.furnaceCookTime = 88;
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(2, 2), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        TileEntityFurnace restored = (TileEntityFurnace) loaded.getChunkBlockTileEntity(4, y, 4);
        int savedState = loaded.getBlockID(4, y, 4) * 100 + loaded.getBlockMetadata(4, y, 4);
        int savedProgress = restored.furnaceBurnTime * 1000 + restored.furnaceCookTime;
        long savedSlots = stack(restored.getStackInSlot(0)) * 100000000L
                + stack(restored.getStackInSlot(1)) * 10000L + stack(restored.getStackInSlot(2));

        int x = 44;
        int z = 36;
        place(world, x, y, z, 62, 4);
        TileEntityFurnace broken = tile(world, x, y, z);
        populate(broken);
        int before = world.loadedEntityList.size();
        Block.stoneOvenActive.dropBlockAsItemWithChance(world, x, y, z, 4, 1.0F);
        world.setBlockWithNotify(x, y, z, 0);
        int[] drops = drops(world, before);
        int breakAfter = state(world, x, y, z);

        int idleCollision = Block.stoneOvenIdle.getCollisionBoundingBoxFromPool(
                world, x, y, z) == null ? 0 : 1;
        int activeCollision = Block.stoneOvenActive.getCollisionBoundingBoxFromPool(
                world, x, y, z) == null ? 0 : 1;
        int light = Block.lightOpacity[61] * 100000 + Block.lightValue[61] * 1000
                + Block.lightOpacity[62] * 100 + Block.lightValue[62];
        FurnaceLifecycleProbe result = new FurnaceLifecycleProbe(breakAfter, drops[0], drops[1],
                savedState, savedProgress, savedSlots, idleCollision, activeCollision, light);
        result.validate();
        return result;
    }

    String lifecycle() {
        return "active=62:4->0:0,drops=61+12+263+20,saved=62:5+burn777+cook88";
    }
    String physics() {
        return "collision=61:full+62:full,light=61:255:0+62:255:13";
    }
    private void validate() {
        FurnaceDomainProbe.require(breakAfter == 0 && dropCount == 4 && dropCode == 35604,
                "active furnace lifecycle drifted: " + dropCount + "/" + dropCode);
        FurnaceDomainProbe.require(savedState == 6205 && savedProgress == 777088,
                "furnace chunk progress drifted: " + savedState + "/" + savedProgress);
        FurnaceDomainProbe.require(savedSlots == 120363012001L,
                "furnace chunk inventory drifted: " + savedSlots);
        FurnaceDomainProbe.require(idleCollision == 1 && activeCollision == 1,
                "furnace collision drifted");
        FurnaceDomainProbe.require(lightCode == 25525513,
                "furnace light drifted: " + lightCode);
        FurnaceDomainProbe.require(Block.stoneOvenActive.idDropped(
                4, new java.util.Random(17320110707L)) == 61,
                "active furnace item route drifted");
    }
    private static void populate(TileEntityFurnace tile) {
        tile.setInventorySlotContents(0, new ItemStack(12, 1, 0));
        tile.setInventorySlotContents(1, new ItemStack(263, 1, 0));
        tile.setInventorySlotContents(2, new ItemStack(20, 1, 0));
    }
    private static TileEntityFurnace tile(World world, int x, int y, int z) {
        return (TileEntityFurnace) world.getBlockTileEntity(x, y, z);
    }
    private static void place(World world, int x, int y, int z, int id, int metadata) {
        FurnaceDomainProbe.require(world.setBlockWithNotify(x, y, z, id),
                "furnace block placement failed");
        world.setBlockMetadataWithNotify(x, y, z, metadata);
    }
    private static int[] drops(World world, int index) {
        int count = 0;
        int sum = 0;
        for (int current = index; current < world.loadedEntityList.size(); current++) {
            Entity entity = (Entity) world.loadedEntityList.get(current);
            if (entity instanceof EntityItem) {
                ItemStack stack = ((EntityItem) entity).item;
                count += stack.stackSize;
                sum += stack.itemID;
            }
        }
        return new int[] {count, sum * 100 + count};
    }
    private static int stack(ItemStack value) {
        return value.itemID * 100 + value.stackSize;
    }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
}
