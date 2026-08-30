package aero.modellib.test;

import java.util.ArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Builds four view-separated panels containing fifteen distinct Aero model types. */
public final class WorldlineM776Rehydrator {
    private WorldlineM776Rehydrator() {}

    public static void rehydrate(World world) {
        clearOutsideFixture(world);
        int[] ids = ids();
        for (int floor = 0; floor < 8; floor++) {
            int y = 72 + floor * 4;
            for (int lane = 0; lane < 4; lane++) {
                replace(world, 20 + lane * 4, y, 8, ids[lane]);
                replace(world, 56, y, 20 + lane * 4, ids[4 + lane]);
                replace(world, 20 + lane * 4, y, 56, ids[8 + lane]);
                if (lane < 3) replace(world, 8, y, 22 + lane * 5, ids[12 + lane]);
            }
        }
    }

    public static int count(World world) {
        int count = 0;
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (!block.isRemoved() && fixture(block.x, block.y, block.z)) count++;
        }
        return count;
    }

    private static void clearOutsideFixture(World world) {
        for (Object value : new ArrayList<Object>(world.blockEntities)) {
            BlockEntity block = (BlockEntity) value;
            if (fixture(block.x, block.y, block.z)) continue;
            world.removeBlockEntity(block.x, block.y, block.z);
        }
    }

    private static boolean fixture(int x, int y, int z) {
        if (y < 72 || y > 100 || (y - 72) % 4 != 0) return false;
        return z == 8 && x >= 20 && x <= 32 && (x - 20) % 4 == 0
            || x == 56 && z >= 20 && z <= 32 && (z - 20) % 4 == 0
            || z == 56 && x >= 20 && x <= 32 && (x - 20) % 4 == 0
            || x == 8 && z >= 22 && z <= 32 && (z - 22) % 5 == 0;
    }

    private static int[] ids() {
        return new int[] {AeroTestMod.megaModelBlock.id,
            AeroTestMod.animatedMegaModelBlock.id, AeroTestMod.motorBlock.id,
            AeroTestMod.pumpBlock.id, AeroTestMod.crystalBlock.id,
            AeroTestMod.crystalChaosBlock.id, AeroTestMod.easingShowcaseBlock.id,
            AeroTestMod.easingShowcase2Block.id, AeroTestMod.easingShowcase3Block.id,
            AeroTestMod.plasmaCrystalBlock.id, AeroTestMod.conveyorBlock.id,
            AeroTestMod.spellCircleBlock.id, AeroTestMod.turretIkBlock.id,
            AeroTestMod.morphCrystalBlock.id, AeroTestMod.graphPoweredBlock.id};
    }

    private static void replace(World world, int x, int y, int z, int id) {
        int actual = world.getBlockId(x, y, z);
        if (actual == id) return;
        if (actual != 0) world.removeBlockEntity(x, y, z);
        world.setBlockWithoutNotifyingNeighbors(x, y, z, id);
    }
}
