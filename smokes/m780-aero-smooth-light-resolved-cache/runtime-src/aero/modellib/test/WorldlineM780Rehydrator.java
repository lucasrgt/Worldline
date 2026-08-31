package aero.modellib.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Restores four multi-chunk panels containing 128 static MegaCrusher models. */
public final class WorldlineM780Rehydrator {
    private static final int FLOOR_BLOCK_ID = 7;

    private WorldlineM780Rehydrator() {}

    public static void rehydrate(World world) {
        clearOutsideFixture(world);
        installFloor(world);
        for (int floor = 0; floor < 4; floor++) {
            int y = 72 + floor * 5;
            for (int lane = 0; lane < 8; lane++) {
                int offset = 12 + lane * 6;
                replace(world, offset, y, 4);
                replace(world, 68, y, offset);
                replace(world, offset, y, 68);
                replace(world, 4, y, offset);
            }
        }
    }

    public static int count(World world) {
        int count = 0;
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (!block.isRemoved() && fixture(block.x, block.y, block.z)
                    && block instanceof MegaModelBlockEntity) count++;
        }
        return count;
    }

    /** True only for one of the 128 controlled fixture positions. */
    public static boolean contains(int x, int y, int z) {
        return fixture(x, y, z);
    }

    /** Stabilizes block-entity draw order across fresh world deserializations. */
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

    private static void clearOutsideFixture(World world) {
        for (Object value : new ArrayList<Object>(world.blockEntities)) {
            BlockEntity block = (BlockEntity) value;
            if (!fixture(block.x, block.y, block.z)) {
                world.removeBlockEntity(block.x, block.y, block.z);
            }
        }
    }

    private static void installFloor(World world) {
        for (int x = -16; x <= 88; x++) {
            for (int z = -16; z <= 88; z++) {
                world.setBlockWithoutNotifyingNeighbors(x, 68, z, FLOOR_BLOCK_ID);
            }
        }
    }

    private static boolean fixture(int x, int y, int z) {
        if (y < 72 || y > 87 || (y - 72) % 5 != 0) return false;
        return z == 4 && lane(x) || x == 68 && lane(z)
            || z == 68 && lane(x) || x == 4 && lane(z);
    }

    private static boolean lane(int value) {
        return value >= 12 && value <= 54 && (value - 12) % 6 == 0;
    }

    private static void replace(World world, int x, int y, int z) {
        int id = AeroTestMod.megaModelBlock.id;
        int actual = world.getBlockId(x, y, z);
        if (actual == id) return;
        if (actual != 0) world.removeBlockEntity(x, y, z);
        world.setBlockWithoutNotifyingNeighbors(x, y, z, id);
    }
}
