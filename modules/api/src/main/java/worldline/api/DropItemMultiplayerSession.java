package worldline.api;

/** Held-item session extended with the original drop-current-item action. */
public interface DropItemMultiplayerSession extends HeldItemMultiplayerSession {
    void dropHeldItem();
}
