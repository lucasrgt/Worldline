package aero.modellib.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Builds four independent 144-machine towers across four chunks. */
public final class WorldlineM784Rehydrator {
    public static final int MACHINES = 576;
    private static final int[] ORIGINS = {1, 1, 33, 1, 1, 33, 33, 33};

    private WorldlineM784Rehydrator() {}

    public static void rehydrate(World world) {
        clearOutsideFixture(world);
        for (int tower = 0; tower < ORIGINS.length; tower += 2) {
            for (int floor = 0; floor < 16; floor++) {
                int y = 64 + floor * 4;
                for (int x = 0; x < 3; x++) {
                    for (int z = 0; z < 3; z++) {
                        replace(world, ORIGINS[tower] + x * 3, y,
                            ORIGINS[tower + 1] + z * 3);
                    }
                }
            }
        }
    }

    public static int count(World world) {
        int count = 0;
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (!block.isRemoved() && contains(block.x, block.y, block.z)
                    && block instanceof MegaModelBlockEntity) count++;
        }
        return count;
    }

    public static boolean contains(int x, int y, int z) {
        if (y < 64 || y > 124 || (y - 64) % 4 != 0) return false;
        for (int tower = 0; tower < ORIGINS.length; tower += 2) {
            if (lane(x, ORIGINS[tower]) && lane(z, ORIGINS[tower + 1])) return true;
        }
        return false;
    }

    public static void order(World world) {
        Collections.sort(world.blockEntities, new Comparator<BlockEntity>() {
            public int compare(BlockEntity left, BlockEntity right) {
                int value = Integer.compare(left.y, right.y);
                if (value == 0) value = Integer.compare(left.x, right.x);
                if (value == 0) value = Integer.compare(left.z, right.z);
                if (value == 0) value = left.getClass().getName()
                    .compareTo(right.getClass().getName());
                return value;
            }
        });
    }

    private static boolean lane(int value, int origin) {
        return value >= origin && value <= origin + 6 && (value - origin) % 3 == 0;
    }

    private static void clearOutsideFixture(World world) {
        for (Object value : new ArrayList<Object>(world.blockEntities)) {
            BlockEntity block = (BlockEntity) value;
            if (!contains(block.x, block.y, block.z)) {
                world.removeBlockEntity(block.x, block.y, block.z);
            }
        }
    }

    private static void replace(World world, int x, int y, int z) {
        int id = AeroTestMod.megaModelBlock.id;
        int actual = world.getBlockId(x, y, z);
        if (actual == id) return;
        if (actual != 0) world.removeBlockEntity(x, y, z);
        world.setBlockWithoutNotifyingNeighbors(x, y, z, id);
    }
}
