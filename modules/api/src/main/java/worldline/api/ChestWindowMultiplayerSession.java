package worldline.api;

/** Block-placement session extended with one bounded chest open/read action. */
public interface ChestWindowMultiplayerSession extends BlockPlacementMultiplayerSession {
    RemoteContainerWindow openChest(BlockPosition position, BlockFace face);
    RemoteWindowClosure closeWindow();
}
