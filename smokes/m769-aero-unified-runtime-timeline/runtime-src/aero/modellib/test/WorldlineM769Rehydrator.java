package aero.modellib.test;

import net.minecraft.world.World;

/** Rehydrates the exact synthetic MEGA matrix over a restored tower save. */
public final class WorldlineM769Rehydrator {
    private static final int BASE_Y = 63;
    private static final int[][] ORIGINS = {{1, 1}, {1, 11}, {11, 1}, {11, 11}};

    private WorldlineM769Rehydrator() {}

    public static void rehydrate(World world) {
        int[] ids = {AeroTestMod.motorBlock.id, AeroTestMod.animatedMegaModelBlock.id,
                AeroTestMod.pumpBlock.id, AeroTestMod.conveyorBlock.id};
        for (int floor = 0; floor < 16; floor++) {
            int y = BASE_Y + floor * 4 + 1;
            for (int cluster = 0; cluster < ORIGINS.length; cluster++) {
                int expected = ids[cluster];
                int ox = ORIGINS[cluster][0];
                int oz = ORIGINS[cluster][1];
                for (int sx = 0; sx < 3; sx++) {
                    for (int sz = 0; sz < 3; sz++) {
                        int x = ox + sx;
                        int z = oz + sz;
                        int actual = world.getBlockId(x, y, z);
                        if (actual != expected && actual != 0) {
                            throw new IllegalStateException("restored M769 block drift at "
                                    + x + "," + y + "," + z + "; expected=" + expected
                                    + " actual=" + actual);
                        }
                        world.removeBlockEntity(x, y, z);
                        world.setBlockWithoutNotifyingNeighbors(x, y, z, 0);
                        world.setBlockWithoutNotifyingNeighbors(x, y, z, expected);
                    }
                }
            }
        }
    }
}
