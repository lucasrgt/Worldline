package worldline.smoke.b173fluidfrozen;

import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import worldline.testapi.FluidFrozenMatterObservation;

/** Proves the remaining fluid, sponge, snow, and ice lifecycle boundaries. */
final class FluidFrozenMatterProbe {
    private static final int[] IDS = {8, 9, 10, 11, 19, 78, 79, 80};
    final int[][] rows;

    private FluidFrozenMatterProbe(int[][] rows) {
        this.rows = rows;
    }

    static FluidFrozenMatterProbe execute(World world) {
        EntityPlayer player = new EntityPlayer(world) { };
        int[][] rows = new int[IDS.length][];
        for (int index = 0; index < IDS.length; index++) {
            int id = IDS[index];
            int x = 8 + index * 5;
            rows[index] = inspect(world, player, x, id);
        }
        FluidFrozenMatterProbe result = new FluidFrozenMatterProbe(rows);
        result.validate();
        return result;
    }

    FluidFrozenMatterObservation observation() {
        return new FluidFrozenMatterObservation(
                "8+10:item-route+consumed,8+9+10+11:break-removed+drop-none,10:meta0-15",
                "19:rate10+direct-tick-stable+neighbor-stable",
                "78:rate10+dark-tick-stable+support-stable+support-loss-air",
                "79:meta0+break-to-water+drop-none+full-collision+neighbor-stable",
                "80:random-enrolled+direct-tick-stable");
    }

    private static int[] inspect(World world, EntityPlayer player, int x, int id) {
        ensure(world, x, 64, 8, 1, "support failed: " + id);
        int placement = fluidPlacement(world, player, x, id);
        int domain = id == 10 ? metadataDomain(world, x, id) : id == 79 ? 1 : -1;
        int lifecycle = fluid(id) ? fluidBreak(world, player, x, id) : -1;
        if (id == 19)
            return sponge(world, x, placement, domain, lifecycle);
        if (id == 78)
            return snowLayer(world, x, placement, domain, lifecycle);
        if (id == 79)
            return ice(world, player, x, placement, domain, lifecycle);
        if (id == 80)
            return snowBlock(world, x, placement, domain, lifecycle);
        return new int[] {id, placement, domain, lifecycle, -1, -1, -1};
    }

    private static int fluidPlacement(World world, EntityPlayer player, int x, int id) {
        if (id != 8 && id != 10)
            return -1;
        ItemStack stack = new ItemStack(id, 1, 0);
        require(Item.itemsList[id] != null, "fluid item route missing: " + id);
        boolean placed = Item.itemsList[id].onItemUse(stack, player, world, x, 64, 8, 1);
        int code = placed && world.getBlockId(x, 65, 8) == id && stack.stackSize == 0 ? 1 : 0;
        world.setBlockWithNotify(x, 65, 8, 0);
        return code;
    }

    private static int metadataDomain(World world, int x, int id) {
        require(world.setBlockAndMetadataWithNotify(x, 65, 8, id, 0), "domain cell failed");
        int mask = 0;
        for (int metadata = 0; metadata <= 15; metadata++) {
            rawMetadata(world, x, 65, 8, metadata);
            require(world.getBlockMetadata(x, 65, 8) == metadata,
                    "metadata rejected: " + id + ":" + metadata);
            mask |= 1 << metadata;
        }
        world.setBlockWithNotify(x, 65, 8, 0);
        return mask;
    }

    private static int fluidBreak(World world, EntityPlayer player, int x, int id) {
        require(world.setBlockAndMetadataWithNotify(x, 65, 8, id, 0), "break cell failed: " + id);
        int entities = world.loadedEntityList.size();
        require(world.setBlockWithNotify(x, 65, 8, 0), "break removal failed: " + id);
        Block.blocksList[id].harvestBlock(world, player, x, 65, 8, 0);
        return world.getBlockId(x, 65, 8) == 0
                && world.loadedEntityList.size() == entities ? 1 : 0;
    }

    private static int[] sponge(World world, int x, int placement, int domain, int lifecycle) {
        require(world.setBlockAndMetadataWithNotify(x, 65, 8, 19, 0), "sponge cell failed");
        int before = state(world, x);
        Block.blocksList[19].updateTick(world, x, 65, 8, new Random(17320110891L));
        int tick = before == state(world, x) && Block.blocksList[19].tickRate() == 10 ? 1 : 0;
        Block.blocksList[19].onNeighborBlockChange(world, x, 65, 8, 1);
        int neighbor = before == state(world, x) ? 1 : 0;
        return new int[] {19, placement, domain, lifecycle, tick, neighbor, -1};
    }

    private static int[] snowLayer(World world, int x, int placement, int domain, int lifecycle) {
        require(world.setBlockAndMetadataWithNotify(x, 65, 8, 78, 0), "snow-layer cell failed");
        int before = state(world, x);
        Block.blocksList[78].updateTick(world, x, 65, 8, new Random(17320110892L));
        int tick = before == state(world, x) && Block.blocksList[78].tickRate() == 10 ? 1 : 0;
        Block.blocksList[78].onNeighborBlockChange(world, x, 65, 8, 1);
        int supported = before == state(world, x) ? 1 : 0;
        world.setBlockWithNotify(x, 64, 8, 0);
        if (world.getBlockId(x, 65, 8) != 0)
            Block.blocksList[78].onNeighborBlockChange(world, x, 65, 8, 0);
        int loss = world.getBlockId(x, 65, 8) == 0 ? 1 : 0;
        return new int[] {78, placement, domain, lifecycle, tick, supported, loss};
    }

    private static int[] ice(World world, EntityPlayer player, int x,
            int placement, int domain, int lifecycle) {
        ensure(world, x, 64, 8, 1, "ice support failed");
        require(world.setBlockAndMetadataWithNotify(x, 65, 8, 79, 0), "ice cell failed");
        AxisAlignedBB box = Block.blocksList[79].getCollisionBoundingBoxFromPool(world, x, 65, 8);
        int collision = full(box, x) ? 1 : 0;
        Block.blocksList[79].onNeighborBlockChange(world, x, 65, 8, 1);
        int neighbor = state(world, x) == 7900 ? 1 : 0;
        int entities = world.loadedEntityList.size();
        world.setBlockWithNotify(x, 65, 8, 0);
        Block.blocksList[79].harvestBlock(world, player, x, 65, 8, 0);
        int broken = world.getBlockId(x, 65, 8);
        int iceBreak = (broken == 8 || broken == 9)
                && world.loadedEntityList.size() == entities ? 1 : 0;
        return new int[] {79, placement, domain, lifecycle, collision, neighbor, iceBreak};
    }

    private static int[] snowBlock(World world, int x,
            int placement, int domain, int lifecycle) {
        require(world.setBlockAndMetadataWithNotify(x, 65, 8, 80, 0), "snow-block cell failed");
        int before = state(world, x);
        Block.blocksList[80].updateTick(world, x, 65, 8, new Random(17320110893L));
        int tick = before == state(world, x) && Block.tickOnLoad[80] ? 1 : 0;
        return new int[] {80, placement, domain, lifecycle, tick, -1, -1};
    }

    private void validate() {
        int[][] expected = {
            {8, 1, -1, 1, -1, -1, -1}, {9, -1, -1, 1, -1, -1, -1},
            {10, 1, 65535, 1, -1, -1, -1}, {11, -1, -1, 1, -1, -1, -1},
            {19, -1, -1, -1, 1, 1, -1}, {78, -1, -1, -1, 1, 1, 1},
            {79, -1, 1, -1, 1, 1, 1}, {80, -1, -1, -1, 1, -1, -1}
        };
        require(rows.length == expected.length, "fluid/frozen row inventory drifted");
        for (int index = 0; index < rows.length; index++)
            require(matches(rows[index], expected[index]),
                    "fluid/frozen row drifted: " + describe(rows[index]));
    }

    private static boolean fluid(int id) {
        return id >= 8 && id <= 11;
    }
    private static int state(World world, int x) {
        return world.getBlockId(x, 65, 8) * 100 + world.getBlockMetadata(x, 65, 8);
    }
    private static void rawMetadata(World world, int x, int y, int z, int metadata) {
        Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
        chunk.setBlockMetadata(x & 15, y, z & 15, metadata);
    }
    private static void ensure(World world, int x, int y, int z, int id, String message) {
        if (world.getBlockId(x, y, z) != id)
            require(world.setBlockAndMetadataWithNotify(x, y, z, id, 0), message);
    }
    private static boolean full(AxisAlignedBB box, int x) {
        return box != null && box.minX == x && box.minY == 65D && box.minZ == 8D
                && box.maxX == x + 1D && box.maxY == 66D && box.maxZ == 9D;
    }
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
