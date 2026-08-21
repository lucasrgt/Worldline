package worldline.b173;

import worldline.api.GamePlayer;
import worldline.api.GameWorld;

/** Capabilities granted to a controlled b1.7.3 mod callback. */
public interface B173ModContext {
    int clientTick();

    int blockAt(int x, int y, int z);

    boolean setBlock(int x, int y, int z, int blockId);

    /** Stable M3 world handle; valid for the lifetime of the runtime. */
    GameWorld world();

    /** Stable M3 local-player handle; valid for the lifetime of the runtime. */
    GamePlayer player();

    /**
     * Schedules {@code action} to run at the start of controlled tick
     * {@code tick}, before that tick's mod callbacks. Past ticks fail closed.
     */
    void at(int tick, Runnable action);
}
