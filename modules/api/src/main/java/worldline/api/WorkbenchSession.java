package worldline.api;

/** Furnace output retrieval extended with typed workbench open/read lifecycle. */
public interface WorkbenchSession extends FurnaceOutputSession {
    RemoteContainerWindow openWorkbench(BlockPosition position, BlockFace face);
}
