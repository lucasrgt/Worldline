package worldline.smoke.b173repeater;

import java.util.ArrayList;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/** Proves active-state lifecycle, chunk persistence, collision, and light. */
final class RepeaterLifecycleProbe {
    final int dropCount, breakAfter, dropStack, savedOff, savedOn;
    final int offBoxes, onBoxes, offHeight, onHeight, lightCode;

    private RepeaterLifecycleProbe(int dropCount, int breakAfter, int dropStack,
            int savedOff, int savedOn, int offBoxes, int onBoxes, int offHeight,
            int onHeight, int lightCode) {
        this.dropCount = dropCount;
        this.breakAfter = breakAfter;
        this.dropStack = dropStack;
        this.savedOff = savedOff;
        this.savedOn = savedOn;
        this.offBoxes = offBoxes;
        this.onBoxes = onBoxes;
        this.offHeight = offHeight;
        this.onHeight = onHeight;
        this.lightCode = lightCode;
    }

    static RepeaterLifecycleProbe execute(World world) {
        int before = world.loadedEntityList.size(), x = 20, y = 88, z = 20;
        world.setBlockAndMetadataWithNotify(x, y - 1, z, 1, 0);
        world.setBlockAndMetadataWithNotify(x, y, z, 94, 15);
        Block.redstoneRepeaterActive.dropBlockAsItemWithChance(world, x, y, z, 15, 1.0F);
        EntityItem item = itemAfter(world, before);
        world.setBlockWithNotify(x, y, z, 0);
        int after = state(world, x, y, z), drop = stack(item);

        int persistentY = 90, persistentZ = 34;
        world.setBlockAndMetadataWithNotify(4, persistentY - 1, persistentZ, 1, 0);
        world.setBlockAndMetadataWithNotify(4, persistentY, persistentZ, 93, 0);
        world.setBlockAndMetadataWithNotify(8, persistentY - 1, persistentZ, 1, 0);
        world.setBlockAndMetadataWithNotify(8, persistentY, persistentZ, 94, 15);
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(0, 2), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int savedOff = loaded.getBlockID(4, persistentY, 2) * 100
                + loaded.getBlockMetadata(4, persistentY, 2);
        int savedOn = loaded.getBlockID(8, persistentY, 2) * 100
                + loaded.getBlockMetadata(8, persistentY, 2);

        int[] offShape = shape(world, Block.redstoneRepeaterIdle, -16, 96, 20);
        int[] onShape = shape(world, Block.redstoneRepeaterActive, -8, 96, 20);
        int light = Block.lightOpacity[93] * 1000 + Block.lightValue[93] * 100
                + Block.lightOpacity[94] * 10 + Block.lightValue[94];
        RepeaterLifecycleProbe result = new RepeaterLifecycleProbe(
                world.loadedEntityList.size() - before, after, drop, savedOff, savedOn,
                offShape[0], onShape[0], offShape[1], onShape[1], light);
        result.validate();
        return result;
    }

    String lifecycle() {
        return "on=94:15->0:0+drop=356x1:0,saved=93:0+94:15";
    }
    String physics() {
        return "collision=93:1/8+94:1/8,light=93:0:0+94:0:9";
    }

    private void validate() {
        RepeaterTimingProbe.require(breakAfter == 0 && dropStack == 3560100,
                "active repeater break/drop drifted");
        RepeaterTimingProbe.require(savedOff == 9300 && savedOn == 9415,
                "repeater chunk round trip drifted");
        RepeaterTimingProbe.require(offBoxes == 1 && onBoxes == 1
                && offHeight == 1 && onHeight == 1,
                "repeater collision envelope drifted");
        RepeaterTimingProbe.require(lightCode == 9,
                "repeater light envelope drifted: " + lightCode);
        RepeaterTimingProbe.require(Block.redstoneRepeaterIdle.idDropped(0, worldRandom()) == 356
                && Block.redstoneRepeaterActive.idDropped(15, worldRandom()) == 356,
                "repeater item route drifted");
    }

    private static int[] shape(World world, Block block, int x, int y, int z) {
        world.setBlockAndMetadataWithNotify(x, y - 1, z, 1, 0);
        world.setBlockAndMetadataWithNotify(x, y, z, block.blockID, 15);
        ArrayList<AxisAlignedBB> values = new ArrayList<AxisAlignedBB>();
        block.getCollidingBoundingBoxes(world, x, y, z,
                AxisAlignedBB.getBoundingBox(x - 1, y - 1, z - 1, x + 2, y + 2, z + 2), values);
        AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, x, y, z);
        int height = (int) Math.round((box.maxY - box.minY) * 8.0D);
        return new int[] {values.size(), height};
    }

    private static java.util.Random worldRandom() { return new java.util.Random(17320110707L); }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static EntityItem itemAfter(World world, int index) {
        for (int current = world.loadedEntityList.size() - 1; current >= index; current--) {
            Entity entity = (Entity) world.loadedEntityList.get(current);
            if (entity instanceof EntityItem) return (EntityItem) entity;
        }
        throw new IllegalStateException("expected active repeater drop was absent");
    }
    private static int stack(EntityItem item) {
        return item.item.itemID * 10000 + item.item.stackSize * 100 + item.item.getItemDamage();
    }
}
