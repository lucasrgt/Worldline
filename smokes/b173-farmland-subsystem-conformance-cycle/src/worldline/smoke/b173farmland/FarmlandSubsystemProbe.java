package worldline.smoke.b173farmland;

import java.util.Random;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/** Proves farmland moisture, placement, harvest, persistence, physics, and cover response. */
final class FarmlandSubsystemProbe {
    final int placementRoute, placedState, stackAfter, metadataMask;
    final int strengthClass, breakBefore, breakAfter, dropDelta, dropId, dropCount, savedState;
    final int collisionFull, visualHeight, opaque, cube, lightCode, tickMask;
    final int hydratedState, dryState, stableState, coverBefore, coverAfter;
    private FarmlandSubsystemProbe(int placementRoute, int placedState, int stackAfter,
            int metadataMask, int strengthClass, int breakBefore, int breakAfter, int dropDelta,
            int dropId, int dropCount, int savedState, int collisionFull, int visualHeight,
            int opaque, int cube, int lightCode, int tickMask, int hydratedState, int dryState,
            int stableState, int coverBefore, int coverAfter) {
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.metadataMask = metadataMask;
        this.strengthClass = strengthClass;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.dropId = dropId;
        this.dropCount = dropCount;
        this.savedState = savedState;
        this.collisionFull = collisionFull;
        this.visualHeight = visualHeight;
        this.opaque = opaque;
        this.cube = cube;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.hydratedState = hydratedState;
        this.dryState = dryState;
        this.stableState = stableState;
        this.coverBefore = coverBefore;
        this.coverAfter = coverAfter;
    }
    static FarmlandSubsystemProbe execute(World world) {
        EntityPlayer player = new EntityPlayer(world) {
        };
        require(world.setBlockAndMetadataWithNotify(20, 79, 20, 1, 0),
                "farmland placement support failed");
        ItemStack stack = new ItemStack(60, 1, 0);
        boolean placed = Item.itemsList[60].onItemUse(stack, player, world, 20, 79, 20, 1);
        int placedState = state(world, 20, 80, 20);

        require(world.setBlockAndMetadataWithNotify(24, 79, 20, 1, 0)
                && world.setBlockAndMetadataWithNotify(24, 80, 20, 60, 0),
                "farmland break cell failed");
        int entities = world.loadedEntityList.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = Block.tilledField.blockStrength(player);
        boolean removed = world.setBlockWithNotify(24, 80, 20, 0);
        if (removed && player.canHarvestBlock(Block.tilledField))
            Block.tilledField.harvestBlock(world, player, 24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);
        EntityItem drop = lastDrop(world, entities);

        require(world.setBlockAndMetadataWithNotify(28, 79, 20, 1, 0)
                && world.setBlockAndMetadataWithNotify(28, 80, 20, 60, 0)
                && world.setBlockAndMetadataWithNotify(29, 80, 20, 9, 0),
                "farmland hydration fixture failed");
        Block.tilledField.updateTick(world, 28, 80, 20, new ZeroRandom());
        int hydrated = state(world, 28, 80, 20);
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(1, 1), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int saved = loaded.getBlockID(12, 80, 4) * 100 + loaded.getBlockMetadata(12, 80, 4);
        world.setBlockWithNotify(29, 80, 20, 0);
        int mask = 1 << world.getBlockMetadata(28, 80, 20);
        for (int step = 0; step < 7; step++) {
            Block.tilledField.updateTick(world, 28, 80, 20, new ZeroRandom());
            mask |= 1 << world.getBlockMetadata(28, 80, 20);
        }
        int dry = state(world, 28, 80, 20);

        AxisAlignedBB box = Block.tilledField.getCollisionBoundingBoxFromPool(
                world, 20, 80, 20);
        int collision = box != null && box.minX == 20D && box.minY == 80D && box.minZ == 20D
                && box.maxX == 21D && box.maxY == 81D && box.maxZ == 21D ? 1 : 0;
        int height = (int) Math.round(Block.tilledField.maxY * 10000D);
        int light = Block.lightOpacity[60] * 100 + Block.lightValue[60];
        Block.tilledField.onNeighborBlockChange(world, 20, 80, 20, 1);
        Block.tilledField.onNeighborBlockChange(world, 20, 80, 20, 69);
        int stable = state(world, 20, 80, 20);
        require(world.setBlockAndMetadataWithNotify(32, 79, 20, 1, 0)
                && world.setBlockAndMetadataWithNotify(32, 80, 20, 60, 0),
                "farmland cover fixture failed");
        int coverBefore = state(world, 32, 80, 20);
        require(world.setBlockAndMetadataWithNotify(32, 81, 20, 1, 0),
                "farmland solid cover failed");
        int coverAfter = state(world, 32, 80, 20);
        FarmlandSubsystemProbe result = new FarmlandSubsystemProbe(placed ? 1 : 0,
                placedState, stack.stackSize, mask,
                strength > 0F && !Float.isInfinite(strength) ? 1 : 0, breakBefore, breakAfter,
                world.loadedEntityList.size() - entities, drop == null ? 0 : drop.item.itemID,
                drop == null ? 0 : drop.item.stackSize, saved, collision, height,
                Block.tilledField.isOpaqueCube() ? 1 : 0,
                Block.tilledField.isACube() ? 1 : 0, light,
                Block.tickOnLoad[60] ? 1 : 0, hydrated, dry, stable, coverBefore, coverAfter);
        result.validate();
        return result;
    }
    String domains() {
        return "60=0..7,item-route=60x1->0,placed=60:0";
    }
    String lifecycle() {
        return "break=60:0->0:0,strength=finite,drop=3x1";
    }
    String persistence() {
        return "chunk-nbt=60:7";
    }
    String physics() {
        return "collision=full,visual-height=15/16,opaque=F,cube=F,light=255:0";
    }
    String timing() {
        return "random-enrolled=T,hydration=0->7,dry=7->0";
    }
    String neighbors() {
        return "air-above=stable-60:0,solid-cover=60:0->3:0";
    }
    private void validate() {
        require(placementRoute == 1 && placedState == 6000 && stackAfter == 0
                && metadataMask == 255, "farmland placement or moisture domain drifted");
        require(strengthClass == 1 && breakBefore == 6000 && breakAfter == 0
                && dropDelta == 1 && dropId == 3 && dropCount == 1,
                "farmland dirt-drop lifecycle drifted");
        require(savedState == 6007, "farmland chunk round trip drifted");
        require(collisionFull == 1 && visualHeight == 9375 && opaque == 0 && cube == 0
                && lightCode == 25500, "farmland physical envelope drifted");
        require(tickMask == 1 && hydratedState == 6007 && dryState == 6000,
                "farmland moisture timing drifted");
        require(stableState == 6000 && coverBefore == 6000 && coverAfter == 300,
                "farmland solid-cover response drifted");
    }
    private static EntityItem lastDrop(World world, int first) {
        for (int index = world.loadedEntityList.size() - 1; index >= first; index--) {
            Object entity = world.loadedEntityList.get(index);
            if (entity instanceof EntityItem)
                return (EntityItem) entity;
        }
        return null;
    }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
    private static final class ZeroRandom extends Random {
        private static final long serialVersionUID = 1L;
        @Override public int nextInt(int bound) {
            return 0;
        }
    }
}
