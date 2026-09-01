package worldline.smoke.b173fire;

import java.util.Random;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/** Proves fire age, placement, harvest, persistence, physics, and support lifecycle. */
final class FireSubsystemProbe {
    final int placementRoute, placedState, stackAfter, metadataMask;
    final int strengthClass, breakBefore, breakAfter, dropDelta, savedState;
    final int collisionNull, collidable, lightCode, tickMask, tickRate;
    final int supportedState, lossBefore, lossAfter;
    private FireSubsystemProbe(int placementRoute, int placedState, int stackAfter,
            int metadataMask, int strengthClass, int breakBefore, int breakAfter, int dropDelta,
            int savedState, int collisionNull, int collidable, int lightCode, int tickMask,
            int tickRate, int supportedState, int lossBefore, int lossAfter) {
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.metadataMask = metadataMask;
        this.strengthClass = strengthClass;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.savedState = savedState;
        this.collisionNull = collisionNull;
        this.collidable = collidable;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickRate = tickRate;
        this.supportedState = supportedState;
        this.lossBefore = lossBefore;
        this.lossAfter = lossAfter;
    }
    static FireSubsystemProbe execute(World world) {
        EntityPlayer player = new EntityPlayer(world) {
        };
        require(world.setBlockAndMetadataWithNotify(20, 79, 20, 87, 0),
                "fire placement support failed");
        ItemStack stack = new ItemStack(51, 1, 0);
        boolean placed = Item.itemsList[51].onItemUse(stack, player, world, 20, 79, 20, 1);
        int placedState = state(world, 20, 80, 20);

        require(world.setBlockAndMetadataWithNotify(24, 79, 20, 87, 0)
                && world.setBlockAndMetadataWithNotify(24, 80, 20, 51, 0),
                "fire break cell failed");
        int entities = world.loadedEntityList.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = Block.fire.blockStrength(player);
        boolean removed = world.setBlockWithNotify(24, 80, 20, 0);
        if (removed && player.canHarvestBlock(Block.fire))
            Block.fire.harvestBlock(world, player, 24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);

        require(world.setBlockAndMetadataWithNotify(28, 79, 20, 87, 0)
                && world.setBlockAndMetadataWithNotify(28, 80, 20, 51, 0),
                "fire persistence cell failed");
        int mask = 1;
        for (int age = 1; age <= 15; age++) {
            Block.fire.updateTick(world, 28, 80, 20, new MaxRandom());
            mask |= 1 << world.getBlockMetadata(28, 80, 20);
        }
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(1, 1), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int saved = loaded.getBlockID(12, 80, 4) * 100 + loaded.getBlockMetadata(12, 80, 4);

        int collisionNull = Block.fire.getCollisionBoundingBoxFromPool(world, 20, 80, 20) == null
                ? 1 : 0;
        int light = Block.lightOpacity[51] * 100 + Block.lightValue[51];
        Block.fire.onNeighborBlockChange(world, 20, 80, 20, 1);
        Block.fire.onNeighborBlockChange(world, 20, 80, 20, 69);
        int supported = state(world, 20, 80, 20);
        require(world.setBlockAndMetadataWithNotify(32, 79, 20, 1, 0)
                && world.setBlockAndMetadataWithNotify(32, 80, 20, 51, 0),
                "fire support-loss cell failed");
        int lossBefore = state(world, 32, 80, 20);
        world.setBlockWithNotify(32, 79, 20, 0);
        int lossAfter = state(world, 32, 80, 20);
        FireSubsystemProbe result = new FireSubsystemProbe(placed ? 1 : 0, placedState,
                stack.stackSize, mask, Float.isInfinite(strength) ? 1 : 0, breakBefore,
                breakAfter, world.loadedEntityList.size() - entities, saved, collisionNull,
                Block.fire.isCollidable() ? 1 : 0, light, Block.tickOnLoad[51] ? 1 : 0,
                Block.fire.tickRate(), supported, lossBefore, lossAfter);
        result.validate();
        return result;
    }
    String domains() {
        return "51=0..15,item-route=51x1->0,placed=51:0";
    }
    String lifecycle() {
        return "break=51:0->0:0,strength=infinite,drop=none";
    }
    String persistence() {
        return "chunk-nbt=51:15";
    }
    String physics() {
        return "collision=none,collidable=F,light=0:15";
    }
    String timing() {
        return "random-enrolled=T,age=0->15,tick-rate=40";
    }
    String neighbors() {
        return "supported=stable-51:0,support-loss=51:0->0:0";
    }
    private void validate() {
        require(placementRoute == 1 && placedState == 5100 && stackAfter == 0
                && metadataMask == 65535, "fire placement or age domain drifted");
        require(strengthClass == 1 && breakBefore == 5100 && breakAfter == 0
                && dropDelta == 0, "fire empty-harvest lifecycle drifted");
        require(savedState == 5115, "fire chunk round trip drifted");
        require(collisionNull == 1 && collidable == 0 && lightCode == 15,
                "fire physical envelope drifted");
        require(tickMask == 1 && tickRate == 40, "fire tick policy drifted");
        require(supportedState == 5100 && lossBefore == 5100 && lossAfter == 0,
                "fire neighbor support lifecycle drifted");
    }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
    private static final class MaxRandom extends Random {
        private static final long serialVersionUID = 1L;
        @Override public int nextInt(int bound) {
            return bound - 1;
        }
    }
}
