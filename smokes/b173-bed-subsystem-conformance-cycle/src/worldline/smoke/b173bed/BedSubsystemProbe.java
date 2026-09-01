package worldline.smoke.b173bed;

import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockBed;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

/** Proves the native two-cell bed state, lifecycle, physics, and neighbor contract. */
final class BedSubsystemProbe {
    final int footMask, headMask, strengthClass;
    final int footBefore, footAfter, footDropId, footDropCount;
    final int headBefore, headAfter, headDropDelta;
    final int collision, height, opaque, cube, lightCode, tickMask;
    final int tickFootBefore, tickFootAfter, tickHeadBefore, tickHeadAfter;
    final int pairFoot, pairHead, orphanFoot, orphanFootDropId, orphanFootDropCount;
    final int orphanHead, orphanHeadDropDelta;
    private BedSubsystemProbe(int footMask, int headMask, int strengthClass,
            int footBefore, int footAfter, int footDropId, int footDropCount,
            int headBefore, int headAfter, int headDropDelta, int collision, int height,
            int opaque, int cube, int lightCode, int tickMask, int tickFootBefore,
            int tickFootAfter, int tickHeadBefore, int tickHeadAfter, int pairFoot,
            int pairHead, int orphanFoot, int orphanFootDropId, int orphanFootDropCount,
            int orphanHead, int orphanHeadDropDelta) {
        this.footMask = footMask;
        this.headMask = headMask;
        this.strengthClass = strengthClass;
        this.footBefore = footBefore;
        this.footAfter = footAfter;
        this.footDropId = footDropId;
        this.footDropCount = footDropCount;
        this.headBefore = headBefore;
        this.headAfter = headAfter;
        this.headDropDelta = headDropDelta;
        this.collision = collision;
        this.height = height;
        this.opaque = opaque;
        this.cube = cube;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickFootBefore = tickFootBefore;
        this.tickFootAfter = tickFootAfter;
        this.tickHeadBefore = tickHeadBefore;
        this.tickHeadAfter = tickHeadAfter;
        this.pairFoot = pairFoot;
        this.pairHead = pairHead;
        this.orphanFoot = orphanFoot;
        this.orphanFootDropId = orphanFootDropId;
        this.orphanFootDropCount = orphanFootDropCount;
        this.orphanHead = orphanHead;
        this.orphanHeadDropDelta = orphanHeadDropDelta;
    }
    static BedSubsystemProbe execute(World world) {
        int footMask = 0, headMask = 0;
        for (int direction = 0; direction < 4; direction++) {
            int x = 44 + direction * 4, z = 20;
            int headX = x + BlockBed.field_22023_a[direction][0];
            int headZ = z + BlockBed.field_22023_a[direction][1];
            require(world.setBlockAndMetadataWithNotify(x, 80, z, 26, direction)
                    && world.setBlockAndMetadataWithNotify(
                            headX, 80, headZ, 26, direction + 8),
                    "bed state-domain pair failed");
            footMask |= 1 << world.getBlockMetadata(x, 80, z);
            headMask |= 1 << world.getBlockMetadata(headX, 80, headZ);
            BlockBed.func_22022_a(world, headX, 80, headZ, true);
            headMask |= 1 << world.getBlockMetadata(headX, 80, headZ);
            BlockBed.func_22022_a(world, headX, 80, headZ, false);
        }

        EntityPlayer player = new EntityPlayer(world) { };
        require(world.setBlockAndMetadataWithNotify(24, 80, 20, 26, 0),
                "bed foot break fixture failed");
        float strength = Block.bed.blockStrength(player);
        int footBefore = state(world, 24, 80, 20);
        int footEntities = world.loadedEntityList.size();
        require(world.setBlockWithNotify(24, 80, 20, 0), "bed foot removal failed");
        Block.bed.harvestBlock(world, player, 24, 80, 20, 0);
        EntityItem footDrop = lastDrop(world, footEntities);
        int footAfter = state(world, 24, 80, 20);

        require(world.setBlockAndMetadataWithNotify(28, 80, 20, 26, 8),
                "bed head break fixture failed");
        int headEntities = world.loadedEntityList.size();
        int headBefore = state(world, 28, 80, 20);
        require(world.setBlockWithNotify(28, 80, 20, 0), "bed head removal failed");
        Block.bed.harvestBlock(world, player, 28, 80, 20, 8);
        int headAfter = state(world, 28, 80, 20);
        int headDropDelta = world.loadedEntityList.size() - headEntities;

        require(world.setBlockAndMetadataWithNotify(20, 80, 20, 26, 0)
                && world.setBlockAndMetadataWithNotify(20, 80, 21, 26, 8),
                "bed physical pair failed");
        AxisAlignedBB box = Block.bed.getCollisionBoundingBoxFromPool(world, 20, 80, 20);
        int collision = box != null && box.minX == 20D && box.minY == 80D
                && box.minZ == 20D && box.maxX == 21D && box.maxY == 80.5625D
                && box.maxZ == 21D ? 1 : 0;
        int tickFootBefore = state(world, 20, 80, 20);
        int tickHeadBefore = state(world, 20, 80, 21);
        Block.bed.updateTick(world, 20, 80, 20, new Random(17320110726L));
        Block.bed.updateTick(world, 20, 80, 21, new Random(17320110726L));

        require(world.setBlockAndMetadataWithNotify(32, 80, 20, 26, 0)
                && world.setBlockAndMetadataWithNotify(32, 80, 21, 26, 8),
                "bed neighbor pair failed");
        Block.bed.onNeighborBlockChange(world, 32, 80, 20, 1);
        Block.bed.onNeighborBlockChange(world, 32, 80, 21, 1);
        int pairFoot = state(world, 32, 80, 20), pairHead = state(world, 32, 80, 21);

        require(world.setBlockAndMetadataWithNotify(36, 80, 20, 26, 0),
                "orphan bed foot failed");
        int orphanFootEntities = world.loadedEntityList.size();
        Block.bed.onNeighborBlockChange(world, 36, 80, 20, 1);
        EntityItem orphanDrop = lastDrop(world, orphanFootEntities);
        require(world.setBlockAndMetadataWithNotify(40, 80, 20, 26, 8),
                "orphan bed head failed");
        int orphanHeadEntities = world.loadedEntityList.size();
        Block.bed.onNeighborBlockChange(world, 40, 80, 20, 1);

        BedSubsystemProbe result = new BedSubsystemProbe(footMask, headMask,
                strength > 0F && !Float.isInfinite(strength) ? 1 : 0,
                footBefore, footAfter, footDrop == null ? 0 : footDrop.item.itemID,
                footDrop == null ? 0 : footDrop.item.stackSize, headBefore, headAfter,
                headDropDelta, collision,
                (int) Math.round(Block.bed.maxY * 10000D),
                Block.bed.isOpaqueCube() ? 1 : 0, Block.bed.isACube() ? 1 : 0,
                Block.lightOpacity[26] * 100 + Block.lightValue[26],
                Block.tickOnLoad[26] ? 1 : 0, tickFootBefore,
                state(world, 20, 80, 20), tickHeadBefore, state(world, 20, 80, 21),
                pairFoot, pairHead, state(world, 36, 80, 20),
                orphanDrop == null ? 0 : orphanDrop.item.itemID,
                orphanDrop == null ? 0 : orphanDrop.item.stackSize,
                state(world, 40, 80, 20), world.loadedEntityList.size() - orphanHeadEntities);
        result.validate();
        return result;
    }
    String domains() {
        return "26=foot:0..3,head:8..15,occupied-head:12..15";
    }
    String lifecycle() {
        return "break=foot+head->air,drops=foot:355x1+head:none,strength=finite";
    }
    String physics() {
        return "collision=1x9/16x1,opaque=F,cube=F,light=0:0";
    }
    String timing() {
        return "scheduled=F,callback-stable=26:0+26:8";
    }
    String neighbors() {
        return "paired=stable,orphan-foot=air+355x1,orphan-head=air+none";
    }
    private void validate() {
        require(footMask == 15 && headMask == 65280, "bed metadata domain drifted");
        require(strengthClass == 1 && footBefore == 2600 && footAfter == 0
                && footDropId == 355 && footDropCount == 1 && headBefore == 2608
                && headAfter == 0 && headDropDelta == 0, "bed break or drop matrix drifted");
        require(collision == 1 && height == 5625 && opaque == 0 && cube == 0
                && lightCode == 0, "bed physical envelope drifted");
        require(tickMask == 0 && tickFootBefore == 2600 && tickFootAfter == 2600
                && tickHeadBefore == 2608 && tickHeadAfter == 2608,
                "bed tick policy drifted");
        require(pairFoot == 2600 && pairHead == 2608 && orphanFoot == 0
                && orphanFootDropId == 355 && orphanFootDropCount == 1
                && orphanHead == 0 && orphanHeadDropDelta == 0,
                "bed neighbor response drifted");
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
