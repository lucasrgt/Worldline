package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.src.Entity;
import net.minecraft.src.World;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GameWorld;

/** Neutral live world handle over the controlled mapped world. */
final class B173World implements GameWorld {
    private final B173ClientBackend owner;
    private final B173Player player;

    B173World(B173ClientBackend owner, B173Player player) { this.owner = owner; this.player = player; }

    @Override public long time() { return world().getWorldTime(); }

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

    private static void require(BlockPosition position) {
        if (position == null) throw new NullPointerException("position");
    }

    private World world() { return owner.client().theWorld; }
}
