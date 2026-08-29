package worldline.smoke.b173piston;

import net.minecraft.src.Block;
import net.minecraft.src.BlockPistonMoving;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntityPiston;
import net.minecraft.src.World;

/** Proves internal piston break/drop delegation and chunk-NBT round trips. */
final class PistonLifecycleProbe {
    final int dropCount, headAfter, baseAfter, headDrop, movingAfter, movingDrop;
    final int savedHead, savedMoving, storedId, storedMetadata, storedDirection;
    final boolean extending;

    private PistonLifecycleProbe(int dropCount, int headAfter, int baseAfter, int headDrop,
            int movingAfter, int movingDrop, int savedHead, int savedMoving, int storedId,
            int storedMetadata, int storedDirection, boolean extending) {
        this.dropCount = dropCount;
        this.headAfter = headAfter;
        this.baseAfter = baseAfter;
        this.headDrop = headDrop;
        this.movingAfter = movingAfter;
        this.movingDrop = movingDrop;
        this.savedHead = savedHead;
        this.savedMoving = savedMoving;
        this.storedId = storedId;
        this.storedMetadata = storedMetadata;
        this.storedDirection = storedDirection;
        this.extending = extending;
    }

    static PistonLifecycleProbe execute(World world) {
        int before = world.loadedEntityList.size();
        int baseX = 0, y = 85, z = 20, headX = baseX + 1;
        boolean priorRemote = world.singleplayerWorld;
        world.singleplayerWorld = true;
        try {
            PistonDomainProbe.require(world.setBlockAndMetadata(baseX, y, z, 33, 13),
                    "extended base fixture placement failed");
            PistonDomainProbe.require(world.setBlockAndMetadata(headX, y, z, 34, 5),
                    "piston head fixture placement failed");
        } finally {
            world.singleplayerWorld = priorRemote;
        }
        world.setBlockWithNotify(headX, y, z, 0);
        int headAfter = PistonDomainProbe.state(world, headX, y, z);
        int baseAfter = PistonDomainProbe.state(world, baseX, y, z);
        EntityItem headItem = itemAfter(world, before, "head after=" + headAfter
                + ",base=" + baseAfter + ",remote=" + world.singleplayerWorld);
        int headDrop = stack(headItem);

        int movingX = 8, movingZ = 20, beforeMoving = world.loadedEntityList.size();
        world.setBlockAndMetadataWithNotify(movingX, y, movingZ, 36, 0);
        world.setBlockTileEntity(movingX, y, movingZ,
                BlockPistonMoving.getTileEntity(1, 0, 5, true, false));
        Block.pistonMoving.dropBlockAsItemWithChance(world, movingX, y, movingZ, 0, 1.0F);
        EntityItem movingItem = itemAfter(world, beforeMoving, "moving state="
                + PistonDomainProbe.state(world, movingX, y, movingZ)
                + ",tile=" + (world.getBlockTileEntity(movingX, y, movingZ) != null)
                + ",remote=" + world.singleplayerWorld);
        world.setBlockWithNotify(movingX, y, movingZ, 0);
        int movingAfter = PistonDomainProbe.state(world, movingX, y, movingZ);
        int movingDrop = stack(movingItem);

        int persistentY = 90, persistentZ = 34;
        world.setBlockAndMetadataWithNotify(4, persistentY, persistentZ, 34, 5);
        world.setBlockAndMetadataWithNotify(8, persistentY, persistentZ, 36, 5);
        world.setBlockTileEntity(8, persistentY, persistentZ,
                BlockPistonMoving.getTileEntity(34, 5, 5, true, false));
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(
                world.getChunkFromChunkCoords(0, 2), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int savedHead = loaded.getBlockID(4, persistentY, 2) * 100
                + loaded.getBlockMetadata(4, persistentY, 2);
        int savedMoving = loaded.getBlockID(8, persistentY, 2) * 100
                + loaded.getBlockMetadata(8, persistentY, 2);
        TileEntityPiston tile = (TileEntityPiston)
                loaded.getChunkBlockTileEntity(8, persistentY, 2);
        PistonDomainProbe.require(tile != null, "moving piston NBT was not restored");
        PistonLifecycleProbe result = new PistonLifecycleProbe(
                world.loadedEntityList.size() - before, headAfter, baseAfter, headDrop,
                movingAfter, movingDrop, savedHead, savedMoving, tile.getStoredBlockID(),
                tile.func_31005_e(), tile.func_31008_d(), tile.func_31010_c());
        result.validate();
        return result;
    }

    String breakAndDrops() {
        return "head=34:5->0:0+base=33:13->0:0+drop=33x1:0,"
                + "moving=36:0->0:0+drop=4x1:0";
    }
    String persistence() {
        return "head=34:5,moving=36:5+te=34:5:5:true";
    }

    private void validate() {
        PistonDomainProbe.require(headAfter == 0 && baseAfter == 0 && headDrop == 330100,
                "piston head delegated break/drop drifted");
        PistonDomainProbe.require(movingAfter == 0 && movingDrop == 40100,
                "moving piston delegated drop drifted: after=" + movingAfter
                        + ",drop=" + movingDrop);
        PistonDomainProbe.require(savedHead == 3405 && savedMoving == 3605
                && storedId == 34 && storedMetadata == 5 && storedDirection == 5 && extending,
                "piston chunk-NBT round trip drifted");
    }
    private static EntityItem itemAfter(World world, int index, String context) {
        for (int current = world.loadedEntityList.size() - 1; current >= index; current--) {
            Entity entity = (Entity) world.loadedEntityList.get(current);
            if (entity instanceof EntityItem)
                return (EntityItem) entity;
        }
        throw new IllegalStateException("expected piston drop was absent: " + context
                + ",before=" + index + ",after=" + world.loadedEntityList.size());
    }
    private static int stack(EntityItem item) {
        return item.item.itemID * 10000 + item.item.stackSize * 100 + item.item.getItemDamage();
    }
}
