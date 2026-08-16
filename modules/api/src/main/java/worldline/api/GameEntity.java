package worldline.api;

/** Stable neutral handle to an active game entity. */
public interface GameEntity {
    int id();

    String type();

    GamePosition position();

    boolean alive();

    void teleport(GamePosition position);
}
