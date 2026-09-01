package worldline.smoke.b173bedrock;

import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/** Proves bedrock placement, unbreakability, persistence, physics, and stability. */
final class BedrockSubsystemProbe {
    final int placementRoute, placedState, stackAfter, metadataMask;
    final int strengthMilli, breakBefore, breakAfter, dropDelta, savedState;
    final int collision, lightCode, tickMask, tickBefore, tickAfter, neighborState;
    private BedrockSubsystemProbe(int placementRoute, int placedState, int stackAfter,
            int metadataMask, int strengthMilli, int breakBefore, int breakAfter, int dropDelta,
            int savedState, int collision, int lightCode, int tickMask, int tickBefore,
            int tickAfter, int neighborState) {
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.metadataMask = metadataMask;
        this.strengthMilli = strengthMilli;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.savedState = savedState;
        this.collision = collision;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickBefore = tickBefore;
        this.tickAfter = tickAfter;
        this.neighborState = neighborState;
    }
    static BedrockSubsystemProbe execute(World world) {
        EntityPlayer player = new EntityPlayer(world) { };
        require(world.setBlockAndMetadataWithNotify(20, 79, 20, 1, 0),
                "bedrock placement support failed");
        ItemStack stack = new ItemStack(7, 1, 0);
        boolean placed = Item.itemsList[7].onItemUse(stack, player, world, 20, 79, 20, 1);
        int placedState = state(world, 20, 80, 20);

        require(world.setBlockAndMetadataWithNotify(24, 80, 20, 7, 0),
                "bedrock break cell failed");
        int entities = world.loadedEntityList.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = Block.bedrock.blockStrength(player);
        if (strength >= 1.0F) {
            world.setBlockWithNotify(24, 80, 20, 0);
            Block.bedrock.harvestBlock(world, player, 24, 80, 20, 0);
        }
        int breakAfter = state(world, 24, 80, 20);

        require(world.setBlockAndMetadataWithNotify(28, 80, 20, 7, 0),
                "bedrock persistence cell failed");
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(1, 1), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int saved = loaded.getBlockID(12, 80, 4) * 100 + loaded.getBlockMetadata(12, 80, 4);

        AxisAlignedBB box = Block.bedrock.getCollisionBoundingBoxFromPool(world, 20, 80, 20);
        int collision = box != null && box.minX == 20D && box.minY == 80D && box.minZ == 20D
                && box.maxX == 21D && box.maxY == 81D && box.maxZ == 21D ? 1 : 0;
        int light = Block.lightOpacity[7] * 100 + Block.lightValue[7];
        int tickBefore = state(world, 20, 80, 20);
        Block.bedrock.updateTick(world, 20, 80, 20, new Random(17320110707L));
        int tickAfter = state(world, 20, 80, 20);
        Block.bedrock.onNeighborBlockChange(world, 24, 80, 20, 1);
        Block.bedrock.onNeighborBlockChange(world, 24, 80, 20, 69);
        BedrockSubsystemProbe result = new BedrockSubsystemProbe(placed ? 1 : 0, placedState,
                stack.stackSize, 1 << world.getBlockMetadata(20, 80, 20),
                Math.round(strength * 1000F), breakBefore, breakAfter,
                world.loadedEntityList.size() - entities, saved, collision, light,
                Block.tickOnLoad[7] ? 1 : 0, tickBefore, tickAfter, state(world, 24, 80, 20));
        result.validate();
        return result;
    }
    String domains() {
        return "7=0,item-route=7x1->0,placed=7:0";
    }
    String lifecycle() {
        return "break-attempt=7:0->7:0,strength=0,drop=none";
    }
    String persistence() {
        return "chunk-nbt=7:0";
    }
    String physics() {
        return "collision=full,light=255:0";
    }
    String timing() {
        return "scheduled=F,callback-stable=7:0";
    }
    String neighbors() {
        return "stone+lever=stable-7:0";
    }
    private void validate() {
        require(placementRoute == 1 && placedState == 700 && stackAfter == 0
                && metadataMask == 1, "bedrock item placement or domain drifted");
        require(strengthMilli == 0 && breakBefore == 700 && breakAfter == 700
                && dropDelta == 0, "bedrock unbreakable lifecycle drifted");
        require(savedState == 700, "bedrock chunk round trip drifted");
        require(collision == 1 && lightCode == 25500, "bedrock physical envelope drifted");
        require(tickMask == 0 && tickBefore == 700 && tickAfter == 700,
                "bedrock tick policy drifted");
        require(neighborState == 700, "bedrock neighbor stability drifted");
    }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
