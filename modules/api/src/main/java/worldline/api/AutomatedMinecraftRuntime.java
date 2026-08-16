package worldline.api;

/** Controlled lifecycle with stable game-domain automation access. */
public interface AutomatedMinecraftRuntime extends MinecraftRuntime {
    GameWorld world();

    GamePlayer player();
}
