package worldline.api;

/** Item-lifecycle session extended with placement of the selected held block. */
public interface BlockPlacementMultiplayerSession extends ItemCollectionMultiplayerSession {
    void placeHeldBlock(BlockPosition support, BlockFace face);
}
