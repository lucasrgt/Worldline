package worldline.api;

/** Block-placement session extended with empty-hand activation of one observed block. */
public interface BlockActivationMultiplayerSession extends BlockPlacementMultiplayerSession {
    void activateBlock(BlockPosition position, BlockFace face);
}
