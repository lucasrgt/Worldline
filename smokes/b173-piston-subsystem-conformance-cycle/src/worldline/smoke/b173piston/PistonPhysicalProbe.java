package worldline.smoke.b173piston;

import java.util.ArrayList;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockPistonMoving;
import net.minecraft.src.World;
import worldline.testapi.TestObservationWindow;

/** Proves piston collision, light, tick policy, and causal neighbor transitions. */
final class PistonPhysicalProbe {
    final int baseBoxes, headBoxes, movingBoxes, lightSum, randomMask, idleTicks;
    final int normalExtended, normalRetracted, stickyExtended, stickyRetracted;
    final int headUnsupported, movingHeld, movingSettled;

    private PistonPhysicalProbe(int baseBoxes, int headBoxes, int movingBoxes,
            int lightSum, int randomMask, int idleTicks, int normalExtended, int normalRetracted,
            int stickyExtended, int stickyRetracted, int headUnsupported,
            int movingHeld, int movingSettled) {
        this.baseBoxes = baseBoxes;
        this.headBoxes = headBoxes;
        this.movingBoxes = movingBoxes;
        this.lightSum = lightSum;
        this.randomMask = randomMask;
        this.idleTicks = idleTicks;
        this.normalExtended = normalExtended;
        this.normalRetracted = normalRetracted;
        this.stickyExtended = stickyExtended;
        this.stickyRetracted = stickyRetracted;
        this.headUnsupported = headUnsupported;
        this.movingHeld = movingHeld;
        this.movingSettled = movingSettled;
    }

    static PistonPhysicalProbe execute(World world) {
        int y = 96;
        int baseBoxes = boxes(world, Block.pistonBase, -20, y, 20, 13, false);
        int headBoxes = boxes(world, Block.pistonExtension, -12, y, 20, 5, false);
        int movingBoxes = boxes(world, Block.pistonMoving, -4, y, 20, 5, true);
        int light = 0, random = 0, index = 0;
        for (int id : new int[] {29, 33, 34, 36}) {
            light += Block.lightOpacity[id] + Block.lightValue[id];
            if (Block.tickOnLoad[id])
                random |= 1 << index;
            index++;
        }

        world.setBlockAndMetadataWithNotify(20, y, 20, 33, 5);
        world.setBlockAndMetadataWithNotify(20, y, 24, 29, 5);
        world.setBlockAndMetadataWithNotify(25, y, 20, 33, 13);
        world.setBlockAndMetadataWithNotify(26, y, 20, 34, 5);
        TestObservationWindow idleWindow = new TestObservationWindow();
        idleWindow.observe(() -> {
            for (int tick = 0; tick < 20; tick++) {
                world.updateEntities();
                world.tick();
            }
            return null;
        }, 20);
        PistonDomainProbe.require(PistonDomainProbe.state(world, 20, y, 20) == 3305
                && PistonDomainProbe.state(world, 20, y, 24) == 2905
                && PistonDomainProbe.state(world, 26, y, 20) == 3405,
                "idle piston tick policy drifted");

        int[] normal = pulse(world, 33, -20, y, 32);
        int[] sticky = pulse(world, 29, -20, y, 40);
        world.setBlockAndMetadataWithNotify(-4, y, 32, 33, 13);
        world.setBlockAndMetadataWithNotify(-3, y, 32, 34, 5);
        world.setBlockWithNotify(-4, y, 32, 0);
        Block.pistonExtension.onNeighborBlockChange(world, -3, y, 32, 0);
        int unsupported = PistonDomainProbe.state(world, -3, y, 32);

        world.setBlockAndMetadataWithNotify(4, y, 32, 36, 5);
        world.setBlockTileEntity(4, y, 32,
                BlockPistonMoving.getTileEntity(34, 5, 5, true, false));
        Block.pistonMoving.onNeighborBlockChange(world, 4, y, 32, 1);
        int held = PistonDomainProbe.state(world, 4, y, 32);
        PistonDomainProbe.require(world.getBlockTileEntity(4, y, 32) != null,
                "moving piston lost its tile on neighbor update");
        PistonDomainProbe.require(PistonDomainProbe.settle(world, 4, y, 32) == 3,
                "moving piston tile duration drifted");
        int settled = PistonDomainProbe.state(world, 4, y, 32);
        PistonPhysicalProbe result = new PistonPhysicalProbe(baseBoxes, headBoxes, movingBoxes,
                light, random, (int) idleWindow.observedTicks(), normal[0], normal[1], sticky[0], sticky[1],
                unsupported, held, settled);
        result.validate();
        return result;
    }

    String collision() {
        return "base=1:full,head=2:plate+rod,moving=1:translated";
    }
    String light() {
        return "29=0:0,33=0:0,34=0:0,36=0:0";
    }
    String ticks() {
        return "random=FFFF,idle=33:5+29:5+34:5@20-window,moving=36:5->34:5@3-te";
    }
    String neighbors() {
        return "normal=33:5->13->5,sticky=29:5->13->5,"
                + "head=34:5->0:0,moving-te=held";
    }

    private void validate() {
        PistonDomainProbe.require(baseBoxes == 1 && headBoxes == 2 && movingBoxes == 1,
                "piston collision family drifted");
        PistonDomainProbe.require(lightSum == 0 && randomMask == 0 && idleTicks == 20,
                "piston light or random tick policy drifted");
        PistonDomainProbe.require(normalExtended == 3313 && normalRetracted == 3305
                && stickyExtended == 2913 && stickyRetracted == 2905,
                "piston neighbor pulse drifted");
        PistonDomainProbe.require(headUnsupported == 0 && movingHeld == 3605
                && movingSettled == 3405, "internal piston transition drifted");
    }

    private static int boxes(World world, Block block, int x, int y, int z,
            int metadata, boolean moving) {
        world.setBlockAndMetadataWithNotify(x, y, z, block.blockID, metadata);
        if (moving)
            world.setBlockTileEntity(x, y, z,
                    BlockPistonMoving.getTileEntity(1, 0, 5, true, false));
        if (moving)
            return block.getCollisionBoundingBoxFromPool(world, x, y, z) == null ? 0 : 1;
        ArrayList<AxisAlignedBB> values = new ArrayList<AxisAlignedBB>();
        block.getCollidingBoundingBoxes(world, x, y, z,
                AxisAlignedBB.getBoundingBox(x - 1, y - 1, z - 1, x + 2, y + 2, z + 2), values);
        return values.size();
    }

    private static int[] pulse(World world, int id, int x, int y, int z) {
        world.setBlockAndMetadataWithNotify(x, y, z, id, 5);
        world.setBlockAndMetadataWithNotify(x - 1, y - 1, z, Block.stone.blockID, 0);
        world.setBlockAndMetadataWithNotify(x - 1, y, z, Block.torchRedstoneActive.blockID, 5);
        Block.blocksList[id].onNeighborBlockChange(
                world, x, y, z, Block.torchRedstoneActive.blockID);
        int extensionSteps = PistonDomainProbe.settle(world, x + 1, y, z);
        PistonDomainProbe.require(extensionSteps == 3,
                "piston extension tile duration drifted: steps=" + extensionSteps
                        + ",base=" + PistonDomainProbe.state(world, x, y, z)
                        + ",front=" + PistonDomainProbe.state(world, x + 1, y, z));
        int extended = PistonDomainProbe.state(world, x, y, z);
        world.setBlockWithNotify(x - 1, y, z, 0);
        Block.blocksList[id].onNeighborBlockChange(world, x, y, z, 0);
        int retractionSteps = PistonDomainProbe.settle(world, x, y, z);
        PistonDomainProbe.require(retractionSteps == 3,
                "piston retraction tile duration drifted: steps=" + retractionSteps
                        + ",base=" + PistonDomainProbe.state(world, x, y, z));
        return new int[] {extended, PistonDomainProbe.state(world, x, y, z)};
    }
}
