package worldline.smoke.b173redstonetorch;

import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/** Proves idle lifecycle, chunk persistence, collision, and light. */
final class RedstoneTorchLifecycleProbe {
    final int dropCount, breakAfter, dropStack, savedOff, savedOn;
    final int offCollision, onCollision, lightCode;
    private RedstoneTorchLifecycleProbe(int dropCount, int breakAfter, int dropStack,
            int savedOff, int savedOn, int offCollision, int onCollision, int lightCode) {
        this.dropCount = dropCount;
        this.breakAfter = breakAfter;
        this.dropStack = dropStack;
        this.savedOff = savedOff;
        this.savedOn = savedOn;
        this.offCollision = offCollision;
        this.onCollision = onCollision;
        this.lightCode = lightCode;
    }
    static RedstoneTorchLifecycleProbe execute(World world) {
        int before = world.loadedEntityList.size(), x = 20, y = 88, z = 32;
        world.setBlockAndMetadataWithNotify(x, y - 1, z, 1, 0);
        world.setBlockAndMetadataWithNotify(x, y, z, 75, 5);
        Block.torchRedstoneIdle.dropBlockAsItemWithChance(world, x, y, z, 5, 1.0F);
        EntityItem item = itemAfter(world, before);
        world.setBlockWithNotify(x, y, z, 0);
        int after = state(world, x, y, z), drop = stack(item);

        int persistentY = 90, persistentZ = 34;
        world.setBlockAndMetadataWithNotify(4, persistentY - 1, persistentZ, 1, 0);
        world.setBlockAndMetadataWithNotify(4, persistentY, persistentZ, 75, 1);
        world.setBlockAndMetadataWithNotify(8, persistentY - 1, persistentZ, 1, 0);
        world.setBlockAndMetadataWithNotify(8, persistentY, persistentZ, 76, 5);
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(0, 2), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int savedOff = loaded.getBlockID(4, persistentY, 2) * 100 + loaded.getBlockMetadata(4, persistentY, 2);
        int savedOn = loaded.getBlockID(8, persistentY, 2) * 100 + loaded.getBlockMetadata(8, persistentY, 2);
        int offCollision = Block.torchRedstoneIdle.getCollisionBoundingBoxFromPool(world, x, y, z) == null ? 0 : 1;
        int onCollision = Block.torchRedstoneActive.getCollisionBoundingBoxFromPool(world, x, y, z) == null ? 0 : 1;
        int light = Block.lightOpacity[75] * 1000 + Block.lightValue[75] * 100
                + Block.lightOpacity[76] * 10 + Block.lightValue[76];
        RedstoneTorchLifecycleProbe result = new RedstoneTorchLifecycleProbe(
                world.loadedEntityList.size() - before, after, drop, savedOff, savedOn,
                offCollision, onCollision, light);
        result.validate();
        return result;
    }
    String lifecycle() {
        return "off=75:5->0:0+drop=76x1:0,saved=75:1+76:5";
    }
    String physics() {
        return "collision=75:none+76:none,light=75:0:0+76:0:7";
    }
    private void validate() {
        RedstoneTorchTimingProbe.require(breakAfter == 0 && dropStack == 760100,
                "idle torch break/drop drifted");
        RedstoneTorchTimingProbe.require(savedOff == 7501 && savedOn == 7605,
                "torch chunk round trip drifted");
        RedstoneTorchTimingProbe.require(offCollision == 0 && onCollision == 0,
                "torch collision drifted");
        RedstoneTorchTimingProbe.require(lightCode == 7, "torch light drifted: " + lightCode);
        RedstoneTorchTimingProbe.require(Block.torchRedstoneIdle.idDropped(
                5, new java.util.Random(17320110707L)) == 76,
                "idle torch item route drifted");
    }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static EntityItem itemAfter(World world, int index) {
        for (int current = world.loadedEntityList.size() - 1; current >= index; current--) {
            Entity entity = (Entity) world.loadedEntityList.get(current);
            if (entity instanceof EntityItem)
                return (EntityItem) entity;
        }
        throw new IllegalStateException("expected idle torch drop was absent");
    }
    private static int stack(EntityItem item) {
        return item.item.itemID * 10000 + item.item.stackSize * 100 + item.item.getItemDamage();
    }
}
