package worldline.kernel;

import worldline.api.GamePlayer;
import worldline.api.GameWorld;
import worldline.api.WorldSource;

/** Narrow integration port implemented by game-specific adapters. */
public interface GameBackend {
    void bootHeadless();

    void loadWorld(WorldSource source);

    void tick();

    default GameWorld world() { throw new UnsupportedOperationException("world automation is unavailable"); }

    default GamePlayer player() { throw new UnsupportedOperationException("player automation is unavailable"); }

    void close();
}
