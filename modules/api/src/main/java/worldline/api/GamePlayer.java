package worldline.api;

/** Stable neutral automation surface for the controlled local player. */
public interface GamePlayer extends GameEntity {
    String username();

    int health();

    int selectedHotbarSlot();

    void selectHotbarSlot(int slot);
}
