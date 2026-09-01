package worldline.smoke.b173vegetation;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.World;

/** Proves native state, shape, light, and neighbor ecology boundaries. */
final class VegetationEcologyProbe {
    private static final int[] IDS = {2, 6, 18, 31, 59, 83};
    final int[][] rows;

    private VegetationEcologyProbe(int[][] rows) {
        this.rows = rows;
    }

    static VegetationEcologyProbe execute(World world) {
        world.singleplayerWorld = false;
        int[][] rows = new int[IDS.length][];
        for (int index = 0; index < IDS.length; index++) {
            int id = IDS[index], x = 8 + index * 4;
            prepareSupport(world, x, id);
            require(world.setBlockWithNotify(x, 65, 8, id),
                    "vegetation placement failed: " + id);
            int domain = domain(world, x, id);
            world.setBlockMetadataWithNotify(x, 65, 8, 0);
            int shape = shape(Block.blocksList[id], world, x, id);
            int light = claimedLight(id) ? Block.lightOpacity[id] * 100
                    + Block.lightValue[id] : -1;
            int neighbor = neighbor(world, x, id);
            rows[index] = new int[] {id, domain, shape, light, neighbor,
                    world.getBlockMetadata(x, 65, 8)};
        }
        VegetationEcologyProbe result = new VegetationEcologyProbe(rows);
        result.validate();
        return result;
    }

    String states() {
        return "2:0,6:0+1+2+8+9+10,18:0+1+2+4+5+6+8+9+10+12+13+14,"
                + "31:0+1+2,59:0-7,83:0-15";
    }

    String shapes() { return "2+18:full,6+59+83:passable"; }

    String light() { return "2:255/0,6+59+83:0/0"; }

    String neighbors() { return "2:stable,6+59:support-drop,18:decay-mark"; }

    private void validate() {
        int[][] expected = {
            {2, 1, 1, 25500, 1, 0},
            {6, 1799, 0, 0, 2, 0},
            {18, 30583, 1, -1, 3, 8},
            {31, 7, -1, -1, -1, 0},
            {59, 255, 0, 0, 2, 0},
            {83, 65535, 0, 0, -1, 0}
        };
        require(rows.length == expected.length, "vegetation row inventory drifted");
        for (int index = 0; index < rows.length; index++)
            require(matches(rows[index], expected[index]),
                    "vegetation row drifted: " + describe(rows[index]));
    }

    private static void prepareSupport(World world, int x, int id) {
        int support = id == 59 ? 60 : (id == 6 || id == 31 || id == 83 ? 3 : 1);
        if (world.getBlockId(x, 64, 8) != support)
            require(world.setBlockWithNotify(x, 64, 8, support),
                    "vegetation support failed: " + id);
        if (id == 83)
            require(world.setBlockWithNotify(x + 1, 64, 8, 9),
                    "sugar cane water failed");
    }

    private static int domain(World world, int x, int id) {
        int[] states;
        if (id == 6)
            states = new int[] {0, 1, 2, 8, 9, 10};
        else if (id == 18)
            states = new int[] {0, 1, 2, 4, 5, 6, 8, 9, 10, 12, 13, 14};
        else if (id == 31)
            states = range(3);
        else if (id == 59)
            states = range(8);
        else if (id == 83)
            states = range(16);
        else
            states = range(1);
        int mask = 0;
        for (int state : states) {
            world.setBlockMetadataWithNotify(x, 65, 8, state);
            require(world.getBlockMetadata(x, 65, 8) == state,
                    "vegetation metadata rejected: " + id + ":" + state);
            mask |= 1 << state;
        }
        return mask;
    }

    private static int[] range(int count) {
        int[] values = new int[count];
        for (int index = 0; index < count; index++)
            values[index] = index;
        return values;
    }

    private static int shape(Block block, World world, int x, int id) {
        if (id == 31)
            return -1;
        AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, x, 65, 8);
        if (id == 2 || id == 18) {
            require(box != null, "full vegetation collision box missing: " + id);
            return scaled(box.minX - x) == 0 && scaled(box.minY - 65D) == 0
                    && scaled(box.minZ - 8D) == 0 && scaled(box.maxX - x) == 160
                    && scaled(box.maxY - 65D) == 160
                    && scaled(box.maxZ - 8D) == 160 ? 1 : -1;
        }
        return box == null ? 0 : -2;
    }

    private static boolean claimedLight(int id) {
        return id == 2 || id == 6 || id == 59 || id == 83;
    }

    private static int neighbor(World world, int x, int id) {
        if (id == 31 || id == 83)
            return -1;
        Block block = Block.blocksList[id];
        if (id == 2) {
            block.onNeighborBlockChange(world, x, 65, 8, 1);
            return world.getBlockId(x, 65, 8) == 2
                    && world.getBlockMetadata(x, 65, 8) == 0 ? 1 : 0;
        }
        if (id == 18) {
            require(world.setBlockWithNotify(x + 1, 65, 8, 17),
                    "leaf support log placement failed");
            require(world.setBlockWithNotify(x + 1, 65, 8, 0),
                    "leaf support log removal failed");
            return world.getBlockId(x, 65, 8) == 18
                    && world.getBlockMetadata(x, 65, 8) == 8 ? 3 : 0;
        }
        require(world.setBlockWithNotify(x, 64, 8, 0),
                "vegetation support removal failed: " + id);
        if (world.getBlockId(x, 65, 8) != 0)
            block.onNeighborBlockChange(world, x, 65, 8, 0);
        return world.getBlockId(x, 65, 8) == 0 ? 2 : 0;
    }

    private static int scaled(double value) { return (int) Math.round(value * 160D); }

    private static boolean matches(int[] actual, int[] expected) {
        if (actual.length != expected.length)
            return false;
        for (int index = 0; index < actual.length; index++)
            if (actual[index] != expected[index])
                return false;
        return true;
    }

    private static String describe(int[] values) {
        StringBuilder result = new StringBuilder();
        for (int value : values) {
            if (result.length() > 0)
                result.append('.');
            result.append(value);
        }
        return result.toString();
    }

    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
