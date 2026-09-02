package aero.modellib.test;

import aero.modellib.Aero_MeshRenderer;
import java.util.ArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Builds two 256-machine layers separated by a two-section occluding slab. */
public final class WorldlineM791Rehydrator {
    private static final int STATIC_BLOCK_ID = 7;
    private static final int LOWER_Y = 70;
    private static final int UPPER_Y = 84;

    private WorldlineM791Rehydrator() {}

    public static void rehydrate(World world) {
        clearBlockEntities(world);
        slab(world, 68);
        slab(world, 79);
        slab(world, 80);
        for (int chunkX = 0; chunkX < 2; chunkX++) {
            for (int chunkZ = 0; chunkZ < 2; chunkZ++) {
                layer(world, chunkX << 4, chunkZ << 4, LOWER_Y, 0);
                layer(world, chunkX << 4, chunkZ << 4, UPPER_Y, 1);
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
        Aero_MeshRenderer.prewarmModel(AnimatedMegaModelBlockEntityRenderer.MODEL);
    }

    private static void layer(World world, int originX, int originZ, int y, int parity) {
        for (int dx = 1; dx < 16; dx += 2) {
            for (int dz = 1; dz < 16; dz += 2) {
                boolean animated = ((dx + dz) / 2 + parity & 1) == 0;
                int id = animated ? AeroTestMod.animatedMegaModelBlock.id
                    : AeroTestMod.megaModelBlock.id;
                replace(world, originX + dx, y, originZ + dz, id);
            }
        }
    }

    private static void slab(World world, int y) {
        for (int x = -32; x < 64; x++) {
            for (int z = -32; z < 64; z++) {
                world.setBlockWithoutNotifyingNeighbors(x, y, z, STATIC_BLOCK_ID);
            }
        }
    }

    private static void clearBlockEntities(World world) {
        for (Object value : new ArrayList<Object>(world.blockEntities)) {
            BlockEntity block = (BlockEntity) value;
            world.removeBlockEntity(block.x, block.y, block.z);
        }
    }

    private static boolean fixture(int x, int y, int z) {
        return (y == LOWER_Y || y == UPPER_Y) && x >= 0 && x < 32 && z >= 0 && z < 32
            && (x & 1) == 1 && (z & 1) == 1;
    }

    private static void replace(World world, int x, int y, int z, int id) {
        if (world.getBlockId(x, y, z) != 0) world.removeBlockEntity(x, y, z);
        world.setBlockWithoutNotifyingNeighbors(x, y, z, id);
    }
}
