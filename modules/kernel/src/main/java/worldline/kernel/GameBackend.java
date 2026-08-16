package worldline.kernel;

import worldline.api.WorldSource;

/** Narrow integration port implemented by game-specific adapters. */
public interface GameBackend {
    void bootHeadless();

    void loadWorld(WorldSource source);

    void tick();

    void close();
}
