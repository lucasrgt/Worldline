package worldline.smoke.lightingmatrixb173;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Captures generated skylight, source attenuation, and queued recovery. */
public final class LightingEngineMatrixBackend implements GameBackend {
  private static final int[] SOURCES = {89, 62, 74, 76};
  private static final int[] EMISSION = {15, 13, 9, 7};
  private final long seed;
  private final List<String> labels = new ArrayList<>();
  private final List<int[]> rows = new ArrayList<>();
  private World world;

  LightingEngineMatrixBackend(long seed) {
    this.seed = seed;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, name), name, seed, null);
    for (int chunkX = -2; chunkX <= 2; chunkX++)
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.getChunkFromChunkCoords(chunkX, chunkZ);
    drain();
  }

  @Override
  public void tick() {
    int open = sky(8, 65, 8), roof = sky(12, 65, 12), aperture = sky(4, 65, 4);
    require(open == 15 && aperture == 15 && roof < 15, "generated skylight matrix drifted");
    add("generated", open, roof, aperture, block(8, 65, 8), block(12, 65, 12));
    for (int index = 0; index < SOURCES.length; index++) {
      change(8, 65, 8, SOURCES[index]);
      int[] light = line(SOURCES[index]);
      require(light[1] == EMISSION[index] && light[2] == EMISSION[index] - 1,
          "source attenuation drifted for " + SOURCES[index]);
      add("source-" + SOURCES[index], light);
    }
    change(8, 65, 8, 0);
    int[] cleared = line(0);
    require(cleared[1] == 0 && cleared[2] == 0 && cleared[3] == 0 && cleared[4] == 0,
        "block light did not recover");
    add("source-clear", cleared);
    int before = sky(4, 65, 4);
    change(4, 68, 4, 1);
    int capped = sky(4, 65, 4);
    change(4, 68, 4, 0);
    int recovered = sky(4, 65, 4);
    require(before == 15 && capped < before && recovered == before, "skylight did not recover");
    add("sky-cap", before, capped, recovered, sky(4, 66, 4), sky(4, 67, 4));
  }

  void record(CanonicalTrace trace) {
    for (int index = 0; index < rows.size(); index++)
      trace.record(labels.get(index), 0L, 0, rows.get(index));
  }

  @Override
  public void close() {
    world = null;
  }

  private int[] line(int source) {
    return new int[] {source, block(8, 65, 8), block(9, 65, 8),
        block(10, 65, 8), block(11, 65, 8)};
  }

  private void change(int x, int y, int z, int id) {
    require(world.setBlockWithNotify(x, y, z, id), "lighting block change rejected");
    drain();
  }

  private void drain() {
    int passes = 0;
    while (world.func_6156_d())
      require(++passes <= 64, "lighting queue did not drain");
  }

  private int sky(int x, int y, int z) {
    return world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z);
  }

  private int block(int x, int y, int z) {
    return world.getSavedLightValue(EnumSkyBlock.Block, x, y, z);
  }

  private void add(String label, int... values) {
    labels.add(label);
    rows.add(values);
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
