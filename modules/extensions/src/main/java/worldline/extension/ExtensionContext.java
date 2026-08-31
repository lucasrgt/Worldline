package worldline.extension;

import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.GamePlayer;
import worldline.api.GameUi;
import worldline.api.GameWorld;

/** TestKit-owned neutral context passed to extension providers. */
public interface ExtensionContext {
    long seed();
    int attempt();
    AutomatedMinecraftRuntime runtime();
    GameWorld world();
    GamePlayer player();
    GameUi ui();
    void tick();
    void tick(int count);
    void attach(String name, byte[] bytes);
    void skip(String reason);
}
