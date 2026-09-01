package worldline.smoke.b173railnetwork;

import java.lang.reflect.Method;
import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockDetectorRail;
import net.minecraft.src.EntityMinecart;
import net.minecraft.src.World;

/** Proves native rail state, geometry, timing, detector, and support rules. */
final class RailNetworkSubsystemProbe {
    final int[] normalRail, poweredRail, detectorRail;
    final int supportMask;

    private RailNetworkSubsystemProbe(int[] normalRail, int[] poweredRail,
            int[] detectorRail, int supportMask) {
        this.normalRail = normalRail;
        this.poweredRail = poweredRail;
        this.detectorRail = detectorRail;
        this.supportMask = supportMask;
    }

    static RailNetworkSubsystemProbe execute(World world) {
        world.singleplayerWorld = false;
        Random random = new Random(17320110660L);

        require(world.setBlockWithNotify(8, 65, 8, 66), "normal rail placement failed");
        int normalDomain = domain(world, 8, 65, 8, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        world.setBlockMetadataWithNotify(8, 65, 8, 0);
        int normalFlat = height(Block.minecartTrack, world, 8, 65, 8);
        world.setBlockMetadataWithNotify(8, 65, 8, 2);
        int normalSlope = height(Block.minecartTrack, world, 8, 65, 8);
        world.setBlockMetadataWithNotify(8, 65, 8, 9);
        Block.minecartTrack.updateTick(world, 8, 65, 8, random);
        int[] normal = row(normalDomain, normalFlat, normalSlope,
                collision(Block.minecartTrack, world, 8, 65, 8), light(66),
                Block.tickOnLoad[66] ? 1 : 0, world.getBlockMetadata(8, 65, 8), 0);

        require(world.setBlockWithNotify(16, 65, 8, 27), "powered rail placement failed");
        int poweredDomain = domain(world, 16, 65, 8,
                new int[] {0, 1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 13});
        world.setBlockMetadataWithNotify(16, 65, 8, 2);
        int poweredSlope = height(Block.railPowered, world, 16, 65, 8);
        world.setBlockMetadataWithNotify(16, 65, 8, 10);
        int poweredBitSlope = height(Block.railPowered, world, 16, 65, 8);
        world.setBlockMetadataWithNotify(16, 65, 8, 8);
        Block.railPowered.updateTick(world, 16, 65, 8, random);
        int[] powered = row(poweredDomain, poweredSlope, poweredBitSlope,
                collision(Block.railPowered, world, 16, 65, 8), light(27),
                Block.tickOnLoad[27] ? 1 : 0, world.getBlockMetadata(16, 65, 8), 0);

        require(world.setBlockWithNotify(24, 65, 8, 28), "detector rail placement failed");
        world.setBlockMetadataWithNotify(24, 65, 8, 2);
        int detectorSlope = height(Block.railDetector, world, 24, 65, 8);
        world.setBlockMetadataWithNotify(24, 65, 8, 10);
        int detectorBitSlope = height(Block.railDetector, world, 24, 65, 8);
        world.setBlockMetadataWithNotify(24, 65, 8, 0);
        int detectorStart = world.getBlockMetadata(24, 65, 8);
        EntityMinecart cart = new EntityMinecart(world, 24.5D, 65D, 8.5D, 0);
        require(world.entityJoinedWorld(cart), "detector minecart join failed");
        AxisAlignedBB trigger = AxisAlignedBB.getBoundingBoxFromPool(
                24.125D, 65D, 8.125D, 24.875D, 65.25D, 8.875D);
        require(world.getEntitiesWithinAABB(EntityMinecart.class, trigger).size() == 1,
                "detector minecart precondition failed");
        evaluate((BlockDetectorRail) Block.railDetector, world, 24, 65, 8, detectorStart);
        int detectorOn = world.getBlockMetadata(24, 65, 8);
        remove(world, cart);
        Block.railDetector.updateTick(world, 24, 65, 8, random);
        int detectorOff = world.getBlockMetadata(24, 65, 8);
        int[] detector = row(detectorStart, detectorOn, detectorOff,
                collision(Block.railDetector, world, 24, 65, 8), light(28),
                Block.tickOnLoad[28] ? 1 : 0, Block.railDetector.tickRate(),
                detectorSlope * 1000 + detectorBitSlope);

        RailNetworkSubsystemProbe result = new RailNetworkSubsystemProbe(
                normal, powered, detector, supportMask(world));
        result.validate();
        return result;
    }

    String normalRail() {
        return "states=0-9,bounds=0:20+2:100,collision=none,light=0/0,tick=stable";
    }

    String poweredRail() {
        return "states=0-5+8-13,bounds=2:100+10:20,collision=none,light=0/0,tick=stable";
    }

    String detectorRail() {
        return "states=0>8>0,bounds=2:100+10:20,collision=none,light=0/0,tick=20";
    }

    String support() { return "27+28+66=air+single-drop"; }

    private void validate() {
        require(matches(normalRail, new int[] {1023, 20, 100, 0, 0, 0, 9, 0}),
                "normal rail contract drifted: " + describe(normalRail));
        require(matches(poweredRail, new int[] {16191, 100, 20, 0, 0, 0, 8, 0}),
                "powered rail contract drifted: " + describe(poweredRail));
        require(matches(detectorRail, new int[] {0, 8, 0, 0, 0, 1, 20, 100020}),
                "detector rail contract drifted: " + describe(detectorRail));
        require(supportMask == 7, "rail support-loss matrix drifted: " + supportMask);
    }

    private static int domain(World world, int x, int y, int z, int[] states) {
        int mask = 0;
        for (int state : states) {
            world.setBlockMetadataWithNotify(x, y, z, state);
            require(world.getBlockMetadata(x, y, z) == state,
                    "rail metadata domain rejected " + state);
            mask |= 1 << state;
        }
        return mask;
    }

    private static int supportMask(World world) {
        int mask = 0;
        mask |= supportLoss(world, 32, 27) ? 1 : 0;
        mask |= supportLoss(world, 40, 28) ? 2 : 0;
        mask |= supportLoss(world, 48, 66) ? 4 : 0;
        return mask;
    }

    private static boolean supportLoss(World world, int x, int id) {
        int before = world.loadedEntityList.size();
        require(world.setBlockWithNotify(x, 65, 8, id), "supported rail placement failed");
        require(world.setBlockWithNotify(x, 64, 8, 0), "rail support removal failed");
        if (world.getBlockId(x, 65, 8) != 0)
            Block.blocksList[id].onNeighborBlockChange(world, x, 65, 8, 0);
        return world.getBlockId(x, 65, 8) == 0
                && world.loadedEntityList.size() - before == 1;
    }

    private static void remove(World world, EntityMinecart cart) {
        cart.setEntityDead();
        world.updateEntities();
        world.loadedEntityList.remove(cart);
    }

    private static int height(Block block, World world, int x, int y, int z) {
        block.setBlockBoundsBasedOnState(world, x, y, z);
        return (int) Math.round(block.maxY * 160D);
    }

    private static int collision(Block block, World world, int x, int y, int z) {
        return block.getCollisionBoundingBoxFromPool(world, x, y, z) == null ? 0 : 1;
    }

    private static int light(int id) {
        return Block.lightOpacity[id] * 100 + Block.lightValue[id];
    }

    private static void evaluate(BlockDetectorRail rail, World world,
            int x, int y, int z, int metadata) {
        try {
            Method method = BlockDetectorRail.class.getDeclaredMethod("func_27035_f",
                    World.class, int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(rail, world, x, y, z, metadata);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("detector evaluator unavailable", failure);
        }
    }

    private static int[] row(int a, int b, int c, int d, int e, int f, int g, int h) {
        return new int[] {a, b, c, d, e, f, g, h};
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
