package worldline.smoke.randomblocks;
import java.util.Random;
import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;
/** Runs isolated grass-spread and stationary-lava ignition updates. */
final class RandomBlocksBackend implements GameBackend {
  private final long seed;
  private final String mode;
  private World world;
  private Random random;
  RandomBlocksBackend(long s, String m) {
    seed = s;
    mode = m;
  }
  public void bootHeadless() {
  }
  public void loadWorld(WorldSource source) {
    String n = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, n), n, seed, null);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        world.getChunkFromChunkCoords(x, z);
    random = new Random(seed);
    if (mode.startsWith("grass"))
      grass();
    else
      lava();
  }
  public void tick() {
    if (mode.startsWith("grass"))
      Block.blocksList[2].updateTick(world, 8, 65, 8, random);
    else
      Block.blocksList[11].updateTick(world, 8, 65, 8, random);
  }
  public void close() {
    world = null;
    random = null;
  }
  private void grass() {
    world.setBlockWithNotify(8, 65, 8, 2);
    for (int x = 7; x <= 9; x++)
      for (int z = 7; z <= 9; z++)
        if (x != 8 || z != 8) {
          world.setBlockWithNotify(x, 65, z, mode.equals("grass-stone") ? 1 : 3);
          world.setBlockWithNotify(x, 66, z, mode.equals("grass-roof") ? 1 : 0);
        }
  }
  private void lava() {
    world.setBlockWithNotify(8, 65, 8, 11);
    int material = mode.equals("lava-planks") ? 5 : mode.equals("lava-wool") ? 35 : 1;
    for (int x = 7; x <= 9; x++)
      for (int z = 7; z <= 9; z++)
        if (x != 8 || z != 8)
          world.setBlockWithNotify(x, 65, z, material);
  }
  void snapshot(CanonicalTrace t, String l) {
    t.record(l, 0, 0, count(2, 7, 9, 62, 66, 7, 9), count(3, 7, 9, 62, 66, 7, 9),
        count(51, 6, 10, 66, 68, 6, 10), world.getBlockId(8, 65, 8));
  }
  void assertOutcome() {
    int grass = count(2, 7, 9, 62, 66, 7, 9), fire = count(51, 6, 10, 66, 68, 6, 10);
    if (mode.equals("grass-open"))
      req(grass > 1, "grass did not spread");
    if (mode.startsWith("grass-") && !mode.equals("grass-open"))
      req(grass == 1, "ineligible grass spread");
    if (mode.equals("lava-planks") || mode.equals("lava-wool"))
      req(fire > 0, "lava did not ignite flammable fixture");
    if (mode.equals("lava-stone"))
      req(fire == 0, "lava ignited stone");
  }
  private int count(int id, int xa, int xb, int ya, int yb, int za, int zb) {
    int n = 0;
    for (int x = xa; x <= xb; x++)
      for (int y = ya; y <= yb; y++)
        for (int z = za; z <= zb; z++)
          if (world.getBlockId(x, y, z) == id)
            n++;
    return n;
  }
  private static void req(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
