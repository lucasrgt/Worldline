package worldline.smoke.b173mobspawner;

import net.minecraft.src.Block;
import net.minecraft.src.BlockMobSpawner;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntityMobSpawner;
import net.minecraft.src.World;

/** Proves mob-spawner registry, lifecycle, persistence, activation, and stability. */
final class MobSpawnerSubsystemProbe {
    final int registryMask, placementRoute, placedState, stackAfter, placedTile;
    final int strengthClass, breakBefore, breakAfter, dropDelta;
    final int savedState, savedEntity, savedDelay, tickMask, farDelay, nearDelay;
    final int neighborState, neighborEntity, neighborDelay;
    private MobSpawnerSubsystemProbe(int registryMask, int placementRoute, int placedState,
            int stackAfter, int placedTile, int strengthClass, int breakBefore, int breakAfter,
            int dropDelta, int savedState, int savedEntity, int savedDelay, int tickMask,
            int farDelay, int nearDelay, int neighborState, int neighborEntity, int neighborDelay) {
        this.registryMask = registryMask;
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.placedTile = placedTile;
        this.strengthClass = strengthClass;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.savedState = savedState;
        this.savedEntity = savedEntity;
        this.savedDelay = savedDelay;
        this.tickMask = tickMask;
        this.farDelay = farDelay;
        this.nearDelay = nearDelay;
        this.neighborState = neighborState;
        this.neighborEntity = neighborEntity;
        this.neighborDelay = neighborDelay;
    }
    static MobSpawnerSubsystemProbe execute(World world) {
        EntityPlayer player = new EntityPlayer(world) { };
        int registry = Block.blocksList[52] == Block.mobSpawner
                && Block.mobSpawner instanceof BlockMobSpawner ? 1 : 0;
        registry |= Item.itemsList[52] instanceof ItemBlock ? 2 : 0;

        require(world.setBlockAndMetadataWithNotify(20, 79, 20, 1, 0),
                "mob-spawner placement support failed");
        ItemStack stack = new ItemStack(52, 1, 0);
        boolean placed = Item.itemsList[52].onItemUse(stack, player, world, 20, 79, 20, 1);
        TileEntityMobSpawner placedEntity = tile(world, 20, 80, 20);
        registry |= placedEntity != null ? 4 : 0;
        int placedTile = matches(placedEntity, "Pig", 20);

        require(world.setBlockAndMetadataWithNotify(24, 80, 20, 52, 0),
                "mob-spawner break fixture failed");
        int entities = world.loadedEntityList.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = Block.mobSpawner.blockStrength(player);
        require(world.setBlockWithNotify(24, 80, 20, 0), "mob-spawner removal failed");
        Block.mobSpawner.harvestBlock(world, player, 24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);
        int dropDelta = world.loadedEntityList.size() - entities;

        require(world.setBlockAndMetadataWithNotify(36, 92, 36, 52, 0),
                "mob-spawner persistence fixture failed");
        TileEntityMobSpawner savedTile = tile(world, 36, 92, 36);
        savedTile.setMobID("Zombie");
        savedTile.delay = 37;
        NBTTagCompound chunkTag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(2, 2), world, chunkTag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, chunkTag);
        TileEntityMobSpawner restored =
                (TileEntityMobSpawner) loaded.getChunkBlockTileEntity(4, 92, 4);
        int savedState = loaded.getBlockID(4, 92, 4) * 100 + loaded.getBlockMetadata(4, 92, 4);

        require(world.setBlockAndMetadataWithNotify(20, 80, 28, 52, 0),
                "mob-spawner tick fixture failed");
        TileEntityMobSpawner ticking = tile(world, 20, 80, 28);
        ticking.updateEntity();
        int farDelay = ticking.delay;
        player.setPosition(20.5D, 80.5D, 28.5D);
        require(world.entityJoinedWorld(player), "mob-spawner player fixture failed");
        ticking.updateEntity();
        int nearDelay = ticking.delay;

        require(world.setBlockAndMetadataWithNotify(32, 80, 20, 52, 0),
                "mob-spawner neighbor fixture failed");
        Block.mobSpawner.onNeighborBlockChange(world, 32, 80, 20, 1);
        Block.mobSpawner.onNeighborBlockChange(world, 32, 80, 20, 69);
        TileEntityMobSpawner neighbor = tile(world, 32, 80, 20);

        MobSpawnerSubsystemProbe result = new MobSpawnerSubsystemProbe(registry,
                placed ? 1 : 0, state(world, 20, 80, 20), stack.stackSize, placedTile,
                strength > 0F && !Float.isInfinite(strength) ? 1 : 0, breakBefore, breakAfter,
                dropDelta, savedState, matches(restored, "Zombie", 37), restored.delay,
                Block.tickOnLoad[52] ? 1 : 0, farDelay, nearDelay,
                state(world, 32, 80, 20), matches(neighbor, "Pig", 20), neighbor.delay);
        result.validate();
        return result;
    }
    String registry() {
        return "block=52:BlockMobSpawner,item=52:ItemBlock,tile=TileEntityMobSpawner";
    }
    String placement() { return "item=52x1->0,placed=52:0,tile=Pig:20"; }
    String lifecycle() { return "break=52:0->0:0,strength=finite,drops=none"; }
    String persistence() { return "chunk-nbt=52:0+Zombie:37"; }
    String timing() { return "scheduled=F,out-of-range=20,near-player=19"; }
    String neighbors() { return "stone+lever=stable-52:0+Pig:20"; }
    private void validate() {
        require(registryMask == 7, "mob-spawner registry drifted");
        require(placementRoute == 1 && placedState == 5200 && stackAfter == 0
                && placedTile == 1, "mob-spawner item placement drifted");
        require(strengthClass == 1 && breakBefore == 5200 && breakAfter == 0
                && dropDelta == 0, "mob-spawner break or drop drifted");
        require(savedState == 5200 && savedEntity == 1 && savedDelay == 37,
                "mob-spawner chunk NBT drifted");
        require(tickMask == 0 && farDelay == 20 && nearDelay == 19,
                "mob-spawner activation tick drifted");
        require(neighborState == 5200 && neighborEntity == 1 && neighborDelay == 20,
                "mob-spawner neighbor stability drifted");
    }
    private static int matches(TileEntityMobSpawner tile, String id, int delay) {
        if (tile == null || tile.delay != delay)
            return 0;
        NBTTagCompound tag = new NBTTagCompound();
        tile.writeToNBT(tag);
        return id.equals(tag.getString("EntityId")) ? 1 : 0;
    }
    private static TileEntityMobSpawner tile(World world, int x, int y, int z) {
        return (TileEntityMobSpawner) world.getBlockTileEntity(x, y, z);
    }
    private static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
