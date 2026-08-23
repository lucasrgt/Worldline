package worldline.smoke.wirecrossing;
import net.minecraft.src.*;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;
final class WireCrossingBackend implements GameBackend {
  private static final int Y = 65;
  private final long seed;
  private World world;
  WireCrossingBackend(long s) {
    seed = s;
  }
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }
  public void loadWorld(WorldSource source) {
    String n = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, n), n, seed, null);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        world.getChunkFromChunkCoords(x, z);
  }
  public void tick() {
    requireWorld().tick();
  }
  public void close() {
    world = null;
  }
  void fixture() {
    World w = requireWorld();
    for (int z = 6; z <= 9; z++)
      require(w.setBlockWithNotify(10, Y + 1, z, Block.stone.blockID), "bridge support failed");
    require(w.setBlockAndMetadataWithNotify(8, Y, 8, Block.torchRedstoneActive.blockID, 5),
        "lower torch failed");
    for (int x = 9; x <= 11; x++)
      wire(x, Y, 8);
    for (int z = 7; z <= 9; z++)
      wire(10, Y + 2, z);
  }
  void disconnectLower() {
    require(requireWorld().setBlockWithNotify(10, Y, 8, 0), "lower connector removal failed");
  }
  void powerUpper() {
    World w = requireWorld();
    require(w.setBlockWithNotify(8, Y, 8, 0), "lower source removal failed");
    require(w.setBlockAndMetadataWithNotify(10, Y + 2, 6, Block.torchRedstoneActive.blockID, 5),
        "upper torch failed");
  }
  void snapshot(CanonicalTrace t, String label) {
    World w = requireWorld();
    t.record(label, w.getWorldTime(), w.loadedEntityList.size(), w.getBlockId(8, Y, 8),
        w.getBlockMetadata(9, Y, 8), w.getBlockId(10, Y, 8), w.getBlockMetadata(11, Y, 8),
        w.getBlockId(10, Y + 2, 6), w.getBlockMetadata(10, Y + 2, 7),
        w.getBlockMetadata(10, Y + 2, 8), w.getBlockMetadata(10, Y + 2, 9));
  }
  void assertFinal() {
    World w = requireWorld();
    require(w.getBlockMetadata(9, Y, 8) == 0 && w.getBlockId(10, Y, 8) == 0
            && w.getBlockMetadata(11, Y, 8) == 0 && w.getBlockMetadata(10, Y + 2, 7) > 0
            && w.getBlockMetadata(10, Y + 2, 8) > 0 && w.getBlockMetadata(10, Y + 2, 9) > 0,
        "crossing isolation drifted");
  }
  private void wire(int x, int y, int z) {
    require(requireWorld().setBlockWithNotify(x, y, z, Block.redstoneWire.blockID),
        "wire placement failed");
  }
  private World requireWorld() {
    if (world == null)
      throw new IllegalStateException("world absent");
    return world;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
