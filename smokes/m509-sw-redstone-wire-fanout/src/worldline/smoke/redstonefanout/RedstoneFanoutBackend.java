package worldline.smoke.redstonefanout;
import net.minecraft.src.*;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;
/** One exact torch-fed T fan-out with branch removal and source removal. */
final class RedstoneFanoutBackend implements GameBackend {
  private static final int TX = 8, SX = 9, JX = 10, Y = 65, Z = 8;
  private final long seed;
  private World world;
  RedstoneFanoutBackend(long s) {
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
    require(world.getBlockId(TX, 64, Z) == Block.stone.blockID, "fixture stone missing");
  }
  public void tick() {
    requireWorld().tick();
  }
  public void close() {
    world = null;
  }
  void power() {
    World w = requireWorld();
    require(w.setBlockAndMetadataWithNotify(TX, Y, Z, Block.torchRedstoneActive.blockID, 5),
        "torch placement failed");
    wire(SX, Z);
    wire(JX, Z);
    wire(JX, Z - 1);
    wire(JX, Z + 1);
  }
  void disconnect() {
    require(requireWorld().setBlockWithNotify(JX, Y, Z + 1, 0), "branch removal failed");
  }
  void depower() {
    require(requireWorld().setBlockWithNotify(TX, Y, Z, 0), "source removal failed");
  }
  void snapshot(CanonicalTrace t, String label) {
    World w = requireWorld();
    t.record(label, w.getWorldTime(), w.loadedEntityList.size(), w.getBlockId(TX, Y, Z),
        w.getBlockMetadata(SX, Y, Z), w.getBlockMetadata(JX, Y, Z), w.getBlockId(JX, Y, Z - 1),
        w.getBlockMetadata(JX, Y, Z - 1), w.getBlockId(JX, Y, Z + 1),
        w.getBlockMetadata(JX, Y, Z + 1));
  }
  void assertFinal() {
    World w = requireWorld();
    require(w.getBlockMetadata(SX, Y, Z) == 0 && w.getBlockMetadata(JX, Y, Z) == 0
            && w.getBlockMetadata(JX, Y, Z - 1) == 0 && w.getBlockId(JX, Y, Z + 1) == 0,
        "fan-out did not depower");
  }
  private void wire(int x, int z) {
    require(requireWorld().setBlockWithNotify(x, Y, z, Block.redstoneWire.blockID),
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
