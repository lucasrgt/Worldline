package worldline.api;

import java.util.List;

/** Stable neutral automation surface for a loaded world. */
public interface GameWorld {
    long time();

    BlockState block(BlockPosition position);

    boolean setBlock(BlockPosition position, BlockState state);

    List<GameEntity> entities();
}
