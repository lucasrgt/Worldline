package worldline.smoke.b173furnace;

import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntityFurnace;
import net.minecraft.src.World;

/** Proves both furnace state domains and the complete native smelting transition. */
final class FurnaceDomainProbe {
    final int idleMask, activeMask, ignition, progress, completion, extinction;
    final int neighborCode, tickMask;

    private FurnaceDomainProbe(int idleMask, int activeMask, int ignition, int progress,
            int completion, int extinction, int neighborCode, int tickMask) {
        this.idleMask = idleMask;
        this.activeMask = activeMask;
        this.ignition = ignition;
        this.progress = progress;
        this.completion = completion;
        this.extinction = extinction;
        this.neighborCode = neighborCode;
        this.tickMask = tickMask;
    }

    static FurnaceDomainProbe execute(World world) {
        int idleMask = 0;
        int activeMask = 0;
        for (int metadata = 2; metadata <= 5; metadata++) {
            int x = -32 + metadata * 8;
            int y = 84;
            int z = -24;
            place(world, x, y, z, 61, metadata);
            require(state(world, x, y, z) == 6100 + metadata,
                    "idle furnace state drifted " + metadata);
            idleMask |= 1 << metadata;
            place(world, x, y, z, 62, metadata);
            require(state(world, x, y, z) == 6200 + metadata,
                    "active furnace state drifted " + metadata);
            activeMask |= 1 << metadata;
        }

        int x = 20;
        int y = 90;
        int z = 20;
        place(world, x, y, z, 61, 4);
        TileEntityFurnace ignitionTile = tile(world, x, y, z);
        ignitionTile.setInventorySlotContents(0, new ItemStack(12, 1, 0));
        ignitionTile.setInventorySlotContents(1, new ItemStack(263, 1, 0));
        ignitionTile.updateEntity();
        int ignition = state(world, x, y, z);
        int progress = ignitionTile.furnaceBurnTime * 10000 + ignitionTile.furnaceCookTime;
        require(ignition == 6204 && progress == 16000001,
                "furnace ignition drifted: " + ignition + "/" + progress);

        int completeX = 24;
        place(world, completeX, y, z, 61, 3);
        TileEntityFurnace completeTile = tile(world, completeX, y, z);
        completeTile.setInventorySlotContents(0, new ItemStack(12, 1, 0));
        completeTile.setInventorySlotContents(1, new ItemStack(263, 1, 0));
        for (int tick = 0; tick < 200; tick++) {
            completeTile.updateEntity();
        }
        ItemStack output = completeTile.getStackInSlot(2);
        int completion = state(world, completeX, y, z) * 100000
                + stack(output) + completeTile.furnaceBurnTime;
        require(completion == 620303402,
                "complete furnace smelt drifted: " + completion);
        completeTile.furnaceBurnTime = 1;
        completeTile.updateEntity();
        int extinction = state(world, completeX, y, z);
        require(extinction == 6103, "furnace extinction drifted: " + extinction);

        Block.stoneOvenIdle.onNeighborBlockChange(world, completeX, y, z, 1);
        Block.stoneOvenActive.onNeighborBlockChange(world, -16, 84, -24, 1);
        int neighbors = state(world, completeX, y, z) * 10000
                + state(world, -16, 84, -24);
        int ticks = (Block.tickOnLoad[61] ? 1 : 0) | (Block.tickOnLoad[62] ? 2 : 0);
        FurnaceDomainProbe result = new FurnaceDomainProbe(idleMask, activeMask, ignition,
                progress, completion, extinction, neighbors, ticks);
        result.validate();
        return result;
    }

    String domains() {
        return "61=2..5,62=2..5";
    }
    String materialization() {
        return "item61=61:2..5,smelt=61:4>62:4>61:3";
    }
    String timing() {
        return "random=FF,tile=burn1600+cook200,output=20x1:0";
    }
    String neighbors() {
        return "stable=61:3+62:2,orientation=2..5";
    }
    private void validate() {
        require(idleMask == 60 && activeMask == 60, "furnace state domain incomplete");
        require(neighborCode == 61036202 && tickMask == 0,
                "furnace neighbor or tick policy drifted: " + neighborCode + "/" + tickMask);
    }
    private static TileEntityFurnace tile(World world, int x, int y, int z) {
        return (TileEntityFurnace) world.getBlockTileEntity(x, y, z);
    }
    private static void place(World world, int x, int y, int z, int id, int metadata) {
        require(world.setBlockWithNotify(x, y, z, id), "furnace block placement failed");
        world.setBlockMetadataWithNotify(x, y, z, metadata);
    }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static int stack(ItemStack value) {
        return value.itemID * 100 + value.stackSize;
    }
    static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
