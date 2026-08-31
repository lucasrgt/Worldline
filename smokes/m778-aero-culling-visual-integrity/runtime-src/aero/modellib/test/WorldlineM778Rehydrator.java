package aero.modellib.test;

import java.util.ArrayList;
import aero.modellib.Aero_MeshRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Builds four view-separated panels containing one complex static Aero model. */
public final class WorldlineM778Rehydrator {
    private static final int STATIC_BLOCK_ID = 7;

    private WorldlineM778Rehydrator() {}

    public static void rehydrate(World world) {
        clearOutsideFixture(world);
        installStaticEnclosure(world);
        int id = AeroTestMod.megaModelBlock.id;
        for (int floor = 0; floor < 8; floor++) {
            int y = 72 + floor * 4;
            for (int lane = 0; lane < 4; lane++) {
                replace(world, 20 + lane * 4, y, 8, id);
                replace(world, 56, y, 20 + lane * 4, id);
                replace(world, 20 + lane * 4, y, 56, id);
                if (lane < 3) replace(world, 8, y, 22 + lane * 5, id);
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

    public static void prewarm() {
        Aero_MeshRenderer.prewarmModel(MegaModelBlockEntityRenderer.MODEL);
    }

    private static void clearOutsideFixture(World world) {
        for (Object value : new ArrayList<Object>(world.blockEntities)) {
            BlockEntity block = (BlockEntity) value;
            if (fixture(block.x, block.y, block.z)) continue;
            world.removeBlockEntity(block.x, block.y, block.z);
        }
    }

    private static void installStaticEnclosure(World world) {
        for (int x = -32; x < 112; x++) {
            for (int z = -32; z < 112; z++) {
                world.setBlockWithoutNotifyingNeighbors(x, 68, z, STATIC_BLOCK_ID);
            }
        }
        for (int y = 69; y < 128; y++) {
            for (int x = -32; x < 112; x++) {
                world.setBlockWithoutNotifyingNeighbors(x, y, -32, STATIC_BLOCK_ID);
                world.setBlockWithoutNotifyingNeighbors(x, y, 111, STATIC_BLOCK_ID);
            }
            for (int z = -31; z < 111; z++) {
                world.setBlockWithoutNotifyingNeighbors(-32, y, z, STATIC_BLOCK_ID);
                world.setBlockWithoutNotifyingNeighbors(111, y, z, STATIC_BLOCK_ID);
            }
        }
    }

    private static boolean fixture(int x, int y, int z) {
        if (y < 72 || y > 100 || (y - 72) % 4 != 0) return false;
        return z == 8 && x >= 20 && x <= 32 && (x - 20) % 4 == 0
            || x == 56 && z >= 20 && z <= 32 && (z - 20) % 4 == 0
            || z == 56 && x >= 20 && x <= 32 && (x - 20) % 4 == 0
            || x == 8 && z >= 22 && z <= 32 && (z - 22) % 5 == 0;
    }

    private static void replace(World world, int x, int y, int z, int id) {
        int actual = world.getBlockId(x, y, z);
        if (actual == id) return;
        if (actual != 0) world.removeBlockEntity(x, y, z);
        world.setBlockWithoutNotifyingNeighbors(x, y, z, id);
    }
}
