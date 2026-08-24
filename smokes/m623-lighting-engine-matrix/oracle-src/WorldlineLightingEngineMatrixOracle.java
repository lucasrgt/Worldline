import java.util.ArrayList;
import java.util.List;
import worldline.trace.CanonicalTrace;

/** Executes the lighting matrix directly against the official server JAR. */
public final class WorldlineLightingEngineMatrixOracle {
  private static final long SEED = 62320260823L;
  private static final int[] SOURCES = {89, 62, 74, 76};
  private static final int[] EMISSION = {15, 13, 9, 7};
  private final List<String> labels = new ArrayList<>();
  private final List<int[]> rows = new ArrayList<>();
  private dj world;

  public static void main(String[] arguments) {
    new WorldlineLightingEngineMatrixOracle().run();
  }

  private void run() {
    System.setProperty("java.awt.headless", "true");
    world = new dj(new OracleMemorySaveHandler(SEED, "lighting-matrix"),
        "lighting-matrix", SEED, null);
    for (int chunkX = -2; chunkX <= 2; chunkX++)
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++)
        world.c(chunkX, chunkZ);
    drain();
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
    CanonicalTrace trace = new CanonicalTrace(SEED);
    for (int index = 0; index < rows.size(); index++)
      trace.record(labels.get(index), 0L, 0, rows.get(index));
    trace.emitTo(System.out);
  }

  private int[] line(int source) {
    return new int[] {source, block(8, 65, 8), block(9, 65, 8),
        block(10, 65, 8), block(11, 65, 8)};
  }

  private void change(int x, int y, int z, int id) {
    require(world.e(x, y, z, id), "lighting block change rejected");
    drain();
  }

  private void drain() {
    int passes = 0;
    while (world.f())
      require(++passes <= 64, "lighting queue did not drain");
  }

  private int sky(int x, int y, int z) {
    return world.a(co.a, x, y, z);
  }

  private int block(int x, int y, int z) {
    return world.a(co.b, x, y, z);
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
