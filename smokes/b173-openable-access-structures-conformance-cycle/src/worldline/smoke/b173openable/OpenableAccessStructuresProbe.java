package worldline.smoke.b173openable;

import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockDoor;
import net.minecraft.src.Chunk;
import net.minecraft.src.EntityItem;
import net.minecraft.src.World;
import worldline.testapi.OpenableAccessStructuresObservation;

/** Proves the final chest, wooden-door, and trapdoor census boundaries. */
final class OpenableAccessStructuresProbe {
    final int[][] rows;

    private OpenableAccessStructuresProbe(int[][] rows) { this.rows = rows; }

    static OpenableAccessStructuresProbe execute(World world) {
        int[][] rows = {chest(world), woodenDoor(world), trapdoor(world)};
        OpenableAccessStructuresProbe result = new OpenableAccessStructuresProbe(rows);
        result.validate();
        return result;
    }

    OpenableAccessStructuresObservation observation() {
        return new OpenableAccessStructuresObservation(
                "54:scheduled=F+callback-stable+neighbor-stable",
                "64:collision=closed-x-3/16+open-z-3/16,light=0:0,"
                        + "scheduled=F+callback-stable",
                "96:meta=0..7,collision=closed-floor-3/16+open-four-faces,light=0:0,"
                        + "scheduled=F+callback-stable,"
                        + "neighbor=support-stable+support-loss-air+96x1");
    }

    private static int[] chest(World world) {
        require(world.setBlockAndMetadataWithNotify(12, 80, 20, 54, 0), "chest fixture failed");
        int before = state(world, 12, 80, 20);
        Block.chest.updateTick(world, 12, 80, 20, new Random(17320110901L));
        int after = state(world, 12, 80, 20);
        Block.chest.onNeighborBlockChange(world, 12, 80, 20, 1);
        return new int[] {54, Block.tickOnLoad[54] ? 1 : 0,
                before, after, state(world, 12, 80, 20)};
    }

    private static int[] woodenDoor(World world) {
        placeDoor(world, 20);
        AxisAlignedBB closed = Block.doorWood.getCollisionBoundingBoxFromPool(world, 20, 80, 20);
        int closedCollision = exact(closed, 20D, 80D, 20D, 20.1875D, 81D, 21D);
        ((BlockDoor) Block.doorWood).func_272_a(world, 20, 80, 20, true);
        AxisAlignedBB open = Block.doorWood.getCollisionBoundingBoxFromPool(world, 20, 80, 20);
        int openCollision = exact(open, 20D, 80D, 20D, 21D, 81D, 20.1875D);
        ((BlockDoor) Block.doorWood).func_272_a(world, 20, 80, 20, false);
        int lowerBefore = state(world, 20, 80, 20), upperBefore = state(world, 20, 81, 20);
        Block.doorWood.updateTick(world, 20, 80, 20, new Random(17320110902L));
        Block.doorWood.updateTick(world, 20, 81, 20, new Random(17320110902L));
        return new int[] {64, closedCollision, openCollision,
                Block.doorWood.isOpaqueCube() ? 1 : 0, Block.doorWood.isACube() ? 1 : 0,
                Block.lightOpacity[64] * 100 + Block.lightValue[64],
                Block.tickOnLoad[64] ? 1 : 0, lowerBefore, state(world, 20, 80, 20),
                upperBefore, state(world, 20, 81, 20)};
    }

    private static int[] trapdoor(World world) {
        require(world.setBlockAndMetadataWithNotify(32, 80, 20, 96, 0),
                "trapdoor domain fixture failed");
        int domain = 0;
        for (int metadata = 0; metadata <= 7; metadata++) {
            rawMetadata(world, 32, 80, 20, metadata);
            require(world.getBlockMetadata(32, 80, 20) == metadata,
                    "trapdoor metadata rejected: " + metadata);
            domain |= 1 << metadata;
        }
        rawMetadata(world, 32, 80, 20, 0);
        int closed = exact(Block.trapdoor.getCollisionBoundingBoxFromPool(world, 32, 80, 20),
                32D, 80D, 20D, 33D, 80.1875D, 21D);
        int faces = trapdoorFaces(world);
        int before = state(world, 32, 80, 20);
        Block.trapdoor.updateTick(world, 32, 80, 20, new Random(17320110903L));
        int after = state(world, 32, 80, 20);

        require(world.setBlockWithNotify(39, 80, 20, 1)
                && world.setBlockAndMetadataWithNotify(40, 80, 20, 96, 3),
                "trapdoor support fixture failed");
        Block.trapdoor.onNeighborBlockChange(world, 40, 80, 20, 1);
        int supported = state(world, 40, 80, 20);
        int entities = world.loadedEntityList.size();
        require(world.setBlockWithNotify(39, 80, 20, 0), "trapdoor support removal failed");
        Block.trapdoor.onNeighborBlockChange(world, 40, 80, 20, 1);
        EntityItem drop = lastDrop(world, entities);
        return new int[] {96, domain, closed, faces,
                Block.trapdoor.isOpaqueCube() ? 1 : 0, Block.trapdoor.isACube() ? 1 : 0,
                Block.lightOpacity[96] * 100 + Block.lightValue[96],
                Block.tickOnLoad[96] ? 1 : 0, before, after, supported,
                state(world, 40, 80, 20), id(drop), count(drop)};
    }

    private static int trapdoorFaces(World world) {
        double[][] boxes = {
            {32D, 80D, 20.8125D, 33D, 81D, 21D},
            {32D, 80D, 20D, 33D, 81D, 20.1875D},
            {32.8125D, 80D, 20D, 33D, 81D, 21D},
            {32D, 80D, 20D, 32.1875D, 81D, 21D}
        };
        int mask = 0;
        for (int direction = 0; direction < boxes.length; direction++) {
            rawMetadata(world, 32, 80, 20, direction + 4);
            double[] box = boxes[direction];
            if (exact(Block.trapdoor.getCollisionBoundingBoxFromPool(world, 32, 80, 20),
                    box[0], box[1], box[2], box[3], box[4], box[5]) == 1)
                mask |= 1 << direction;
        }
        rawMetadata(world, 32, 80, 20, 0);
        return mask;
    }

    private void validate() {
        require(matches(rows[0], new int[] {54, 0, 5400, 5400, 5400}),
                "chest timing or neighbor response drifted");
        require(matches(rows[1], new int[] {64, 1, 1, 0, 0, 0, 0,
                6400, 6400, 6408, 6408}), "wooden-door physical or tick envelope drifted");
        require(matches(rows[2], new int[] {96, 255, 1, 15, 0, 0, 0, 0,
                9600, 9600, 9603, 0, 96, 1}), "trapdoor subsystem drifted");
    }

    private static void placeDoor(World world, int x) {
        require(world.setBlockWithNotify(x, 79, 20, 1)
                && world.setBlockAndMetadataWithNotify(x, 80, 20, 64, 0)
                && world.setBlockAndMetadataWithNotify(x, 81, 20, 64, 8),
                "wooden-door pair failed");
    }
    private static void rawMetadata(World world, int x, int y, int z, int metadata) {
        Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
        chunk.setBlockMetadata(x & 15, y, z & 15, metadata);
    }
    private static int exact(AxisAlignedBB box, double a, double b, double c,
            double d, double e, double f) {
        return box != null && box.minX == a && box.minY == b && box.minZ == c
                && box.maxX == d && box.maxY == e && box.maxZ == f ? 1 : 0;
    }
    private static EntityItem lastDrop(World world, int first) {
        for (int index = world.loadedEntityList.size() - 1; index >= first; index--)
            if (world.loadedEntityList.get(index) instanceof EntityItem)
                return (EntityItem) world.loadedEntityList.get(index);
        return null;
    }
    private static int id(EntityItem drop) { return drop == null ? 0 : drop.item.itemID; }
    private static int count(EntityItem drop) { return drop == null ? 0 : drop.item.stackSize; }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static boolean matches(int[] actual, int[] expected) {
        if (actual.length != expected.length) return false;
        for (int index = 0; index < actual.length; index++)
            if (actual[index] != expected[index]) return false;
        return true;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
