package worldline.smoke.b173builtenvironment;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.World;

/** Proves the native state, shape, light, tick, and neighbor material matrix. */
final class BuiltEnvironmentMaterialsProbe {
    private static final int[] IDS = {1, 17, 20, 30, 43, 44, 53,
            65, 67, 85, 86, 88, 89, 91};
    final int[][] rows;

    private BuiltEnvironmentMaterialsProbe(int[][] rows) {
        this.rows = rows;
    }

    static BuiltEnvironmentMaterialsProbe execute(World world) {
        world.singleplayerWorld = false;
        int[][] rows = new int[IDS.length][];
        Random random = new Random(17320110850L);
        for (int index = 0; index < IDS.length; index++) {
            int id = IDS[index], x = 8 + index * 4;
            if (id == 65)
                require(world.setBlockWithNotify(x, 65, 9, 1), "ladder support failed");
            require(world.setBlockWithNotify(x, 65, 8, id), "material placement failed: " + id);
            int domain = domain(world, x, id);
            int metadata = baseMetadata(id);
            world.setBlockMetadataWithNotify(x, 65, 8, metadata);
            Block block = Block.blocksList[id];
            int shape = shape(block, world, x, id);
            int light = Block.lightOpacity[id] * 100 + Block.lightValue[id];
            int tickOnLoad = Block.tickOnLoad[id] ? 1 : 0;
            int beforeTick = world.getBlockMetadata(x, 65, 8);
            block.updateTick(world, x, 65, 8, random);
            int tickStable = world.getBlockId(x, 65, 8) == id
                    && world.getBlockMetadata(x, 65, 8) == beforeTick ? 1 : 0;
            int neighbor = neighbor(world, x, id, beforeTick);
            rows[index] = new int[] {id, domain, shape, light,
                    tickOnLoad, tickStable, neighbor, beforeTick};
        }
        BuiltEnvironmentMaterialsProbe result = new BuiltEnvironmentMaterialsProbe(rows);
        result.validate();
        return result;
    }

    String states() { return "1:0+17:0-2+20:0+44:0-3+85:0+89:0"; }

    String shapes() { return "17+20+86+89+91:full,65:wall-2/16,67:two-box-stair"; }

    String light() { return "1+17+44+53+67+86:255/0,65+85:0/0"; }

    String ticks() {
        return "86+91:on-load-stable,17+20+30+43+44+53+65+67+85+88+89:manual-stable";
    }

    String neighbors() {
        return "65:support-drop,17+20+30+43+44+53+67+85+86+88+89+91:stable";
    }

    private void validate() {
        int[][] expected = {
            {1, 1, -1, 25500, 0, 1, -1, 0},
            {17, 7, 1, 25500, 0, 1, 1, 2},
            {20, 1, 1, 0, 0, 1, 1, 0},
            {30, -1, -1, 100, 0, 1, 1, 0},
            {43, -1, -1, 25500, 0, 1, 1, 0},
            {44, 15, -1, 25500, 0, 1, 1, 3},
            {53, -1, -1, 25500, 0, 1, 1, 2},
            {65, -1, 2, 0, 0, 1, 2, 2},
            {67, -1, 3, 25500, 0, 1, 1, 2},
            {85, 1, -1, 0, 0, 1, 1, 0},
            {86, -1, 1, 25500, 1, 1, 1, 0},
            {88, -1, -1, 25500, 0, 1, 1, 0},
            {89, 1, 1, 25515, 0, 1, 1, 0},
            {91, -1, 1, 25515, 1, 1, 1, 0}
        };
        require(rows.length == expected.length, "material row inventory drifted");
        for (int index = 0; index < rows.length; index++)
            require(matches(rows[index], expected[index]),
                    "material row drifted: " + describe(rows[index]));
    }

    private static int domain(World world, int x, int id) {
        int limit = id == 17 ? 3 : (id == 44 ? 4 :
                (id == 1 || id == 20 || id == 85 || id == 89 ? 1 : 0));
        if (limit == 0)
            return -1;
        int mask = 0;
        for (int state = 0; state < limit; state++) {
            world.setBlockMetadataWithNotify(x, 65, 8, state);
            require(world.getBlockMetadata(x, 65, 8) == state,
                    "material metadata rejected: " + id + ":" + state);
            mask |= 1 << state;
        }
        return mask;
    }

    private static int baseMetadata(int id) {
        if (id == 17 || id == 53 || id == 65 || id == 67)
            return 2;
        return id == 44 ? 3 : 0;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int shape(Block block, World world, int x, int id) {
        if (id == 67) {
            ArrayList boxes = new ArrayList();
            AxisAlignedBB query = AxisAlignedBB.getBoundingBoxFromPool(
                    x - 1D, 64D, 7D, x + 2D, 67D, 10D);
            block.getCollidingBoundingBoxes(world, x, 65, 8, query, boxes);
            return boxes.size() == 2 ? 3 : -3;
        }
        if (id != 17 && id != 20 && id != 65 && id != 86 && id != 89 && id != 91)
            return -1;
        AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, x, 65, 8);
        require(box != null, "material collision box missing: " + id);
        if (id == 65)
            return scaled(box.minZ - 8D) == 140 && scaled(box.maxZ - 8D) == 160 ? 2 : -2;
        return scaled(box.minX - x) == 0 && scaled(box.minY - 65D) == 0
                && scaled(box.minZ - 8D) == 0 && scaled(box.maxX - x) == 160
                && scaled(box.maxY - 65D) == 160 && scaled(box.maxZ - 8D) == 160 ? 1 : -1;
    }

    private static int neighbor(World world, int x, int id, int metadata) {
        Block block = Block.blocksList[id];
        if (id == 1)
            return -1;
        if (id == 65) {
            int before = world.loadedEntityList.size();
            require(world.setBlockWithNotify(x, 65, 9, 0), "ladder support removal failed");
            if (world.getBlockId(x, 65, 8) != 0)
                block.onNeighborBlockChange(world, x, 65, 8, 0);
            return world.getBlockId(x, 65, 8) == 0
                    && world.loadedEntityList.size() - before == 1 ? 2 : 0;
        }
        block.onNeighborBlockChange(world, x, 65, 8, 1);
        return world.getBlockId(x, 65, 8) == id
                && world.getBlockMetadata(x, 65, 8) == metadata ? 1 : 0;
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
