package worldline.smoke.b173irondoor;

import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockDoor;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

/** Proves the native two-cell iron-door lifecycle and physical contract. */
final class IronDoorSubsystemProbe {
    final int lowerMask, upperMask, strengthClass;
    final int lowerBefore, lowerAfter, lowerDropId, lowerDropCount;
    final int upperBefore, upperAfter, upperDropDelta;
    final int closedCollision, openCollision, opaque, cube, lightCode, tickMask;
    final int tickLowerBefore, tickLowerAfter, tickUpperBefore, tickUpperAfter;
    final int pairLower, pairUpper, orphanLower, orphanLowerDropId, orphanLowerDropCount;
    final int orphanUpper, orphanUpperDropDelta, supportLower, supportUpper;
    final int supportDropId, supportDropCount;
    private IronDoorSubsystemProbe(int lowerMask, int upperMask, int strengthClass,
            int lowerBefore, int lowerAfter, int lowerDropId, int lowerDropCount,
            int upperBefore, int upperAfter, int upperDropDelta, int closedCollision,
            int openCollision, int opaque, int cube, int lightCode, int tickMask,
            int tickLowerBefore, int tickLowerAfter, int tickUpperBefore, int tickUpperAfter,
            int pairLower, int pairUpper, int orphanLower, int orphanLowerDropId,
            int orphanLowerDropCount, int orphanUpper, int orphanUpperDropDelta,
            int supportLower, int supportUpper, int supportDropId, int supportDropCount) {
        this.lowerMask = lowerMask; this.upperMask = upperMask;
        this.strengthClass = strengthClass; this.lowerBefore = lowerBefore;
        this.lowerAfter = lowerAfter; this.lowerDropId = lowerDropId;
        this.lowerDropCount = lowerDropCount; this.upperBefore = upperBefore;
        this.upperAfter = upperAfter; this.upperDropDelta = upperDropDelta;
        this.closedCollision = closedCollision; this.openCollision = openCollision;
        this.opaque = opaque; this.cube = cube; this.lightCode = lightCode;
        this.tickMask = tickMask; this.tickLowerBefore = tickLowerBefore;
        this.tickLowerAfter = tickLowerAfter; this.tickUpperBefore = tickUpperBefore;
        this.tickUpperAfter = tickUpperAfter; this.pairLower = pairLower;
        this.pairUpper = pairUpper; this.orphanLower = orphanLower;
        this.orphanLowerDropId = orphanLowerDropId;
        this.orphanLowerDropCount = orphanLowerDropCount; this.orphanUpper = orphanUpper;
        this.orphanUpperDropDelta = orphanUpperDropDelta;
        this.supportLower = supportLower; this.supportUpper = supportUpper;
        this.supportDropId = supportDropId; this.supportDropCount = supportDropCount;
    }
    static IronDoorSubsystemProbe execute(World world) {
        int lowerMask = 0, upperMask = 0;
        for (int direction = 0; direction < 4; direction++) {
            int x = 48 + direction * 3;
            placePair(world, x, direction);
            lowerMask |= 1 << world.getBlockMetadata(x, 80, 20);
            upperMask |= 1 << world.getBlockMetadata(x, 81, 20);
            ((BlockDoor) Block.doorSteel).func_272_a(world, x, 80, 20, true);
            lowerMask |= 1 << world.getBlockMetadata(x, 80, 20);
            upperMask |= 1 << world.getBlockMetadata(x, 81, 20);
        }
        EntityPlayer player = new EntityPlayer(world) { };
        require(world.setBlockAndMetadataWithNotify(24, 80, 20, 71, 0),
                "iron-door lower break fixture failed");
        float strength = Block.doorSteel.blockStrength(player);
        int lowerBefore = state(world, 24, 80, 20), lowerEntities = world.loadedEntityList.size();
        require(world.setBlockWithNotify(24, 80, 20, 0), "iron-door lower removal failed");
        Block.doorSteel.harvestBlock(world, player, 24, 80, 20, 0);
        EntityItem lowerDrop = lastDrop(world, lowerEntities);
        int lowerAfter = state(world, 24, 80, 20);
        require(world.setBlockAndMetadataWithNotify(28, 80, 20, 71, 8),
                "iron-door upper break fixture failed");
        int upperEntities = world.loadedEntityList.size(), upperBefore = state(world, 28, 80, 20);
        require(world.setBlockWithNotify(28, 80, 20, 0), "iron-door upper removal failed");
        Block.doorSteel.harvestBlock(world, player, 28, 80, 20, 8);
        int upperAfter = state(world, 28, 80, 20);
        int upperDropDelta = world.loadedEntityList.size() - upperEntities;

        placePair(world, 20, 0);
        AxisAlignedBB closed = Block.doorSteel.getCollisionBoundingBoxFromPool(world, 20, 80, 20);
        int closedCollision = exact(closed, 20D, 80D, 20D, 20.1875D, 81D, 21D);
        ((BlockDoor) Block.doorSteel).func_272_a(world, 20, 80, 20, true);
        AxisAlignedBB open = Block.doorSteel.getCollisionBoundingBoxFromPool(world, 20, 80, 20);
        int openCollision = exact(open, 20D, 80D, 20D, 21D, 81D, 20.1875D);
        ((BlockDoor) Block.doorSteel).func_272_a(world, 20, 80, 20, false);
        int tickLowerBefore = state(world, 20, 80, 20), tickUpperBefore = state(world, 20, 81, 20);
        Block.doorSteel.updateTick(world, 20, 80, 20, new Random(17320110771L));
        Block.doorSteel.updateTick(world, 20, 81, 20, new Random(17320110771L));

        placePair(world, 32, 0); Block.doorSteel.onNeighborBlockChange(world, 32, 80, 20, 1);
        int pairLower = state(world, 32, 80, 20), pairUpper = state(world, 32, 81, 20);
        require(world.setBlockWithNotify(36, 79, 20, 1)
                && world.setBlockAndMetadataWithNotify(36, 80, 20, 71, 0),
                "orphan lower fixture failed");
        int orphanLowerEntities = world.loadedEntityList.size();
        Block.doorSteel.onNeighborBlockChange(world, 36, 80, 20, 1);
        EntityItem orphanLowerDrop = lastDrop(world, orphanLowerEntities);
        require(world.setBlockAndMetadataWithNotify(40, 81, 20, 71, 8),
                "orphan upper fixture failed");
        int orphanUpperEntities = world.loadedEntityList.size();
        Block.doorSteel.onNeighborBlockChange(world, 40, 81, 20, 1);
        int orphanUpperDelta = world.loadedEntityList.size() - orphanUpperEntities;
        placePair(world, 44, 0); int supportEntities = world.loadedEntityList.size();
        require(world.setBlock(44, 79, 20, 0), "support removal failed");
        Block.doorSteel.onNeighborBlockChange(world, 44, 80, 20, 1);
        EntityItem supportDrop = lastDrop(world, supportEntities);

        IronDoorSubsystemProbe result = new IronDoorSubsystemProbe(lowerMask, upperMask,
                strength > 0F && !Float.isInfinite(strength) ? 1 : 0, lowerBefore, lowerAfter,
                id(lowerDrop), count(lowerDrop), upperBefore, upperAfter, upperDropDelta,
                closedCollision, openCollision, Block.doorSteel.isOpaqueCube() ? 1 : 0,
                Block.doorSteel.isACube() ? 1 : 0,
                Block.lightOpacity[71] * 100 + Block.lightValue[71], Block.tickOnLoad[71] ? 1 : 0,
                tickLowerBefore, state(world, 20, 80, 20), tickUpperBefore,
                state(world, 20, 81, 20), pairLower, pairUpper, state(world, 36, 80, 20),
                id(orphanLowerDrop), count(orphanLowerDrop), state(world, 40, 81, 20),
                orphanUpperDelta, state(world, 44, 80, 20), state(world, 44, 81, 20),
                id(supportDrop), count(supportDrop));
        result.validate(); return result;
    }
    String domains() { return "71=lower:0..7,upper:8..15,open-bit=4"; }
    String lifecycle() { return "break=lower+upper->air,drops=lower:330x1+upper:none,strength=finite"; }
    String physics() { return "collision=closed-x-3/16+open-z-3/16,opaque=F,cube=F,light=0:0"; }
    String timing() { return "scheduled=F,callback-stable=71:0+71:8"; }
    String neighbors() { return "paired=stable,orphan-lower=air+330x1,orphan-upper=air+none,support-loss=both-air+330x1"; }
    private void validate() {
        require(lowerMask == 255 && upperMask == 65280, "iron-door metadata domain drifted");
        require(strengthClass == 1 && lowerBefore == 7100 && lowerAfter == 0
                && lowerDropId == 330 && lowerDropCount == 1 && upperBefore == 7108
                && upperAfter == 0 && upperDropDelta == 0, "iron-door lifecycle drifted");
        require(closedCollision == 1 && openCollision == 1 && opaque == 0 && cube == 0
                && lightCode == 0, "iron-door physical envelope drifted");
        require(tickMask == 0 && tickLowerBefore == 7100 && tickLowerAfter == 7100
                && tickUpperBefore == 7108 && tickUpperAfter == 7108, "iron-door tick policy drifted");
        require(pairLower == 7100 && pairUpper == 7108 && orphanLower == 0
                && orphanLowerDropId == 330 && orphanLowerDropCount == 1
                && orphanUpper == 0 && orphanUpperDropDelta == 0 && supportLower == 0
                && supportUpper == 0 && supportDropId == 330 && supportDropCount == 1,
                "iron-door neighbor response drifted");
    }
    private static void placePair(World world, int x, int metadata) {
        require(world.setBlockWithNotify(x, 79, 20, 1)
                && world.setBlockAndMetadataWithNotify(x, 80, 20, 71, metadata)
                && world.setBlockAndMetadataWithNotify(x, 81, 20, 71, metadata + 8),
                "iron-door pair failed");
    }
    private static int exact(AxisAlignedBB box, double a, double b, double c,
            double d, double e, double f) {
        return box != null && box.minX == a && box.minY == b && box.minZ == c
                && box.maxX == d && box.maxY == e && box.maxZ == f ? 1 : 0;
    }
    private static EntityItem lastDrop(World world, int first) {
        for (int index = world.loadedEntityList.size() - 1; index >= first; index--)
            if (world.loadedEntityList.get(index) instanceof EntityItem)
                return (EntityItem) world.loadedEntityList.get(index);
        return null;
    }
    private static int id(EntityItem drop) { return drop == null ? 0 : drop.item.itemID; }
    private static int count(EntityItem drop) { return drop == null ? 0 : drop.item.stackSize; }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
