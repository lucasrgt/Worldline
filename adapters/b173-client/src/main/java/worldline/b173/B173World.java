package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.World;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GamePosition;
import worldline.api.GameWorld;
import worldline.api.ItemCensus;

/** Neutral live world handle over the controlled mapped world. */
final class B173World implements GameWorld {
    private final B173ClientBackend owner;
    private final B173Player player;

    B173World(B173ClientBackend owner, B173Player player) { this.owner = owner; this.player = player; }

    @Override public long time() { return world().getWorldTime(); }

    @Override public boolean peaceful() { return world().difficultySetting <= 0; }

    @Override public BlockState block(BlockPosition position) {
        require(position);
        World world = world();
        return new BlockState(world.getBlockId(position.x(), position.y(), position.z()),
                world.getBlockMetadata(position.x(), position.y(), position.z()));
    }

    @Override public boolean setBlock(BlockPosition position, BlockState state) {
        require(position);
        if (state == null) throw new NullPointerException("state");
        return world().setBlockAndMetadataWithNotify(position.x(), position.y(), position.z(),
                state.legacyId(), state.metadata());
    }

    @Override public List<GameEntity> entities() {
        List<GameEntity> result = new ArrayList<>();
        result.add(player);
        for (Object value : world().loadedEntityList) {
            Entity entity = (Entity) value;
            if (entity != player.entity) result.add(new B173Entity(owner, entity));
        }
        return Collections.unmodifiableList(result);
    }

    @Override public Set<Long> loadedChunks() { return B173Chunks.loaded(world()); }

    @Override public ItemCensus itemsInChunks(Set<Long> chunks) {
        return B173Chunks.items(world(), chunks);
    }

    @Override public ItemCensus blocksInChunks(Set<Long> chunks) {
        return B173Blocks.inChunks(world(), chunks);
    }

    @Override public worldline.api.WearCensus wear() {
        return B173Items.wear(world());
    }

    @Override public ItemCensus blocks() {
        return B173Blocks.census(world(), ((int) Math.floor(player.position().x())) >> 4,
                ((int) Math.floor(player.position().z())) >> 4);
    }

    @Override public ItemCensus items() {
        ItemCensus census = ItemCensus.empty();
        for (Object value : world().loadedEntityList) {
            if (value instanceof EntityItem) census = B173Items.add(census, ((EntityItem) value).item);
            else if (value instanceof IInventory && value != player.entity) {
                census = B173Items.add(census, (IInventory) value);
            }
        }
        for (Object value : world().loadedTileEntityList) {
            if (value instanceof IInventory) census = B173Items.add(census, (IInventory) value);
        }
        return census;
    }

    @Override public GameEntity spawn(String type, GamePosition position) {
        return B173Entities.spawn(owner, world(), type, position);
    }

    @Override public boolean remove(GameEntity entity) {
        if (entity == null) throw new NullPointerException("entity");
        return B173Entities.remove(world(), entity);
    }

    @Override public ItemCensus itemsAt(BlockPosition position) {
        return B173Entities.itemsAt(world(), position);
    }

    private static void require(BlockPosition position) {
        if (position == null) throw new NullPointerException("position");
    }

    private World world() { return owner.client().theWorld; }
}
