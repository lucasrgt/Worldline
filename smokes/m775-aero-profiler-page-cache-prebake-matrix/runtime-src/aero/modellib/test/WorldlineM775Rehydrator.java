package aero.modellib.test;

import net.minecraft.world.World;

/** Rehydrates and mutates the synthetic 576-machine matrix fixture. */
public final class WorldlineM775Rehydrator {
    private static final int[][] ORIGINS = {{1, 1}, {1, 11}, {11, 1}, {11, 11}};

    private WorldlineM775Rehydrator() {}

    public static void rehydrate(World world) {
        int[] ids = {AeroTestMod.motorBlock.id, AeroTestMod.animatedMegaModelBlock.id,
                AeroTestMod.pumpBlock.id, AeroTestMod.conveyorBlock.id};
        for (int floor = 0; floor < 16; floor++) {
            int y = 64 + floor * 4;
            for (int cluster = 0; cluster < ORIGINS.length; cluster++) {
                int ox = ORIGINS[cluster][0], oz = ORIGINS[cluster][1];
                for (int sx = 0; sx < 3; sx++) {
                    for (int sz = 0; sz < 3; sz++) {
                        replace(world, ox + sx, y, oz + sz, ids[cluster]);
                    }
                }
            }
        }
    }

    public static void mutation(World world, boolean present) {
        replace(world, 1, 64, 1, present ? AeroTestMod.motorBlock.id : 0);
    }

    private static void replace(World world, int x, int y, int z, int id) {
        int actual = world.getBlockId(x, y, z);
        if (actual == id) return;
        if (actual != 0) world.removeBlockEntity(x, y, z);
        world.setBlockWithoutNotifyingNeighbors(x, y, z, id);
    }
}
