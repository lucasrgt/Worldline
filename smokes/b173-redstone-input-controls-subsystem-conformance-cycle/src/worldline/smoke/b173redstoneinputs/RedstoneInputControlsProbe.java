package worldline.smoke.b173redstoneinputs;

import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockPressurePlate;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumMobType;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

/** Proves the native state, geometry, light, timing, and support rules of redstone inputs. */
final class RedstoneInputControlsProbe {
    final int[] lever, button, stonePlate, woodenPlate;
    final int supportMask;

    private RedstoneInputControlsProbe(int[] lever, int[] button, int[] stonePlate,
            int[] woodenPlate, int supportMask) {
        this.lever = lever;
        this.button = button;
        this.stonePlate = stonePlate;
        this.woodenPlate = woodenPlate;
        this.supportMask = supportMask;
    }

    static RedstoneInputControlsProbe execute(World world) {
        world.singleplayerWorld = false;
        EntityPlayer player = new EntityPlayer(world) { };
        Random random = new Random(17320110690L);

        require(world.setBlockAndMetadataWithNotify(8, 65, 8, 69, 5),
                "lever state fixture failed");
        int leverStart = world.getBlockMetadata(8, 65, 8);
        require(Block.lever.blockActivated(world, 8, 65, 8, player),
                "lever activation failed");
        int leverOn = world.getBlockMetadata(8, 65, 8);
        Block.lever.updateTick(world, 8, 65, 8, random);
        int leverLatched = world.getBlockMetadata(8, 65, 8);
        int[] leverBounds = bounds(Block.lever, world, 8, 65, 8);
        int[] lever = row(leverStart, leverOn, leverLatched, collision(Block.lever, world, 8, 65, 8),
                light(69), Block.tickOnLoad[69] ? 1 : 0, 0, leverBounds);

        require(world.setBlockWithNotify(11, 65, 8, 1), "button support failed");
        require(world.setBlockAndMetadataWithNotify(12, 65, 8, 77, 1),
                "button state fixture failed");
        int buttonStart = world.getBlockMetadata(12, 65, 8);
        int[] buttonUp = bounds(Block.button, world, 12, 65, 8);
        require(Block.button.blockActivated(world, 12, 65, 8, player),
                "button activation failed");
        int buttonOn = world.getBlockMetadata(12, 65, 8);
        int[] buttonDown = bounds(Block.button, world, 12, 65, 8);
        Block.button.updateTick(world, 12, 65, 8, random);
        int buttonOff = world.getBlockMetadata(12, 65, 8);
        int[] button = row(buttonStart, buttonOn, buttonOff,
                collision(Block.button, world, 12, 65, 8), light(77),
                Block.tickOnLoad[77] ? 1 : 0, Block.button.tickRate(),
                join(buttonUp, buttonDown));

        require(world.setBlockAndMetadataWithNotify(16, 65, 8, 70, 0),
                "stone plate state fixture failed");
        require(Block.pressurePlateStone.blockID == 70
                && Block.pressurePlateStone instanceof BlockPressurePlate,
                "stone plate registry precondition failed");
        require(sensitivity((BlockPressurePlate) Block.pressurePlateStone) == 2,
                "stone plate sensitivity precondition failed");
        int stoneStart = world.getBlockMetadata(16, 65, 8);
        int[] stoneUp = bounds(Block.pressurePlateStone, world, 16, 65, 8);
        EntityItem stoneItem = item(world, 16, 65, 8);
        evaluate((BlockPressurePlate) Block.pressurePlateStone, world, 16, 65, 8);
        int stoneItemState = world.getBlockMetadata(16, 65, 8);
        remove(world, stoneItem);
        player.setPosition(16.5D, 66.62D, 8.5D);
        require(world.entityJoinedWorld(player), "stone plate player join failed");
        AxisAlignedBB stoneTrigger = AxisAlignedBB.getBoundingBoxFromPool(
                16.125D, 65D, 8.125D, 16.875D, 65.25D, 8.875D);
        require(world.getEntitiesWithinAABB(EntityLiving.class, stoneTrigger).size() == 1,
                "stone plate player precondition failed");
        evaluate((BlockPressurePlate) Block.pressurePlateStone, world, 16, 65, 8);
        int stoneOn = world.getBlockMetadata(16, 65, 8);
        int[] stoneDown = bounds(Block.pressurePlateStone, world, 16, 65, 8);
        remove(world, player);
        Block.pressurePlateStone.updateTick(world, 16, 65, 8, random);
        int stoneOff = world.getBlockMetadata(16, 65, 8);
        int[] stone = row(stoneStart, stoneOn, stoneOff,
                collision(Block.pressurePlateStone, world, 16, 65, 8), light(70),
                stoneItemState, Block.pressurePlateStone.tickRate(), join(stoneUp, stoneDown));

        require(world.setBlockAndMetadataWithNotify(20, 65, 8, 72, 0),
                "wooden plate state fixture failed");
        int woodStart = world.getBlockMetadata(20, 65, 8);
        int[] woodUp = bounds(Block.pressurePlatePlanks, world, 20, 65, 8);
        EntityItem woodItem = item(world, 20, 65, 8);
        evaluate((BlockPressurePlate) Block.pressurePlatePlanks, world, 20, 65, 8);
        int woodOn = world.getBlockMetadata(20, 65, 8);
        int[] woodDown = bounds(Block.pressurePlatePlanks, world, 20, 65, 8);
        remove(world, woodItem);
        Block.pressurePlatePlanks.updateTick(world, 20, 65, 8, random);
        int woodOff = world.getBlockMetadata(20, 65, 8);
        int[] wood = row(woodStart, woodOn, woodOff,
                collision(Block.pressurePlatePlanks, world, 20, 65, 8), light(72),
                1, Block.pressurePlatePlanks.tickRate(), join(woodUp, woodDown));

        int support = supportMask(world);
        RedstoneInputControlsProbe result =
                new RedstoneInputControlsProbe(lever, button, stone, wood, support);
        result.validate();
        return result;
    }

    String lever() {
        return "state=5>13>5,bounds=40.0.40.120.96.120,collision=none,light=0/0,tick=latch";
    }

    String button() {
        return "state=1>9>1,bounds=0.60.50.20.100.110>0.60.50.10.100.110,"
                + "collision=none,light=0/0,tick=20";
    }

    String stonePlate() {
        return "state=0>1>0,item=ignored,bounds=10.0.10.150.10.150>10.0.10.150.5.150,"
                + "collision=none,light=0/0,tick=20";
    }

    String woodenPlate() {
        return "state=0>1>0,item=accepted,bounds=10.0.10.150.10.150>10.0.10.150.5.150,"
                + "collision=none,light=0/0,tick=20";
    }

    String support() { return "69+70+72+77=air+single-drop"; }

    private void validate() {
        require(matches(lever, new int[] {5, 13, 13, 0, 0, 0, 0,
                40, 0, 40, 120, 96, 120}),
                "lever contract drifted: " + describe(lever));
        require(matches(button, new int[] {1, 9, 1, 0, 0, 1, 20,
                0, 60, 50, 20, 100, 110, 0, 60, 50, 10, 100, 110}),
                "button contract drifted");
        require(matches(stonePlate, new int[] {0, 1, 0, 0, 0, 0, 20,
                10, 0, 10, 150, 10, 150, 10, 0, 10, 150, 5, 150}),
                "stone pressure-plate contract drifted: " + describe(stonePlate));
        require(matches(woodenPlate, new int[] {0, 1, 0, 0, 0, 1, 20,
                10, 0, 10, 150, 10, 150, 10, 0, 10, 150, 5, 150}),
                "wooden pressure-plate contract drifted: " + describe(woodenPlate));
        require(supportMask == 15, "support-loss matrix drifted");
    }

    private static int supportMask(World world) {
        int mask = 0;
        mask |= supportLoss(world, 28, 69, 5, true) ? 1 : 0;
        mask |= supportLoss(world, 32, 77, 1, false) ? 2 : 0;
        mask |= supportLoss(world, 36, 70, 0, true) ? 4 : 0;
        mask |= supportLoss(world, 40, 72, 0, true) ? 8 : 0;
        return mask;
    }

    private static boolean supportLoss(World world, int x, int id, int metadata, boolean floor) {
        int before = world.loadedEntityList.size();
        if (!floor)
            require(world.setBlockWithNotify(x - 1, 65, 8, 1), "wall support failed");
        require(world.setBlockAndMetadataWithNotify(x, 65, 8, id, metadata),
                "supported control placement failed");
        if (floor)
            require(world.setBlockWithNotify(x, 64, 8, 0), "floor support removal failed");
        else
            require(world.setBlockWithNotify(x - 1, 65, 8, 0), "wall support removal failed");
        if (world.getBlockId(x, 65, 8) != 0)
            Block.blocksList[id].onNeighborBlockChange(world, x, 65, 8, 0);
        return world.getBlockId(x, 65, 8) == 0
                && world.loadedEntityList.size() - before == 1;
    }

    private static EntityItem item(World world, int x, int y, int z) {
        EntityItem item = new EntityItem(world, x + 0.5D, y + 0.1D, z + 0.5D,
                new ItemStack(1, 1, 0));
        require(world.entityJoinedWorld(item), "pressure-plate item join failed");
        return item;
    }

    private static void remove(World world, Entity entity) {
        entity.setEntityDead();
        world.updateEntities();
        world.loadedEntityList.remove(entity);
        world.playerEntities.remove(entity);
    }

    private static int collision(Block block, World world, int x, int y, int z) {
        AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, x, y, z);
        return box == null ? 0 : 1;
    }

    private static int light(int id) {
        return Block.lightOpacity[id] * 100 + Block.lightValue[id];
    }

    private static int[] bounds(Block block, World world, int x, int y, int z) {
        block.setBlockBoundsBasedOnState(world, x, y, z);
        return new int[] {scale(block.minX), scale(block.minY), scale(block.minZ),
                scale(block.maxX), scale(block.maxY), scale(block.maxZ)};
    }

    private static int scale(double value) { return (int) Math.round(value * 160D); }

    private static int sensitivity(BlockPressurePlate plate) {
        try {
            java.lang.reflect.Field field = BlockPressurePlate.class
                    .getDeclaredField("triggerMobType");
            field.setAccessible(true);
            Object value = field.get(plate);
            if (value == EnumMobType.everything)
                return 1;
            if (value == EnumMobType.mobs)
                return 2;
            if (value == EnumMobType.players)
                return 3;
            return 0;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("pressure-plate sensitivity unavailable", failure);
        }
    }

    private static void evaluate(BlockPressurePlate plate, World world, int x, int y, int z) {
        try {
            java.lang.reflect.Method method = BlockPressurePlate.class.getDeclaredMethod(
                    "setStateIfMobInteractsWithPlate", World.class,
                    int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(plate, world, x, y, z);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("pressure-plate evaluator unavailable", failure);
        }
    }

    private static int[] row(int start, int active, int released, int collision, int light,
            int policy, int rate, int[] bounds) {
        int[] result = new int[7 + bounds.length];
        int[] prefix = {start, active, released, collision, light, policy, rate};
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(bounds, 0, result, prefix.length, bounds.length);
        return result;
    }

    private static int[] join(int[] first, int[] second) {
        int[] result = new int[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
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
