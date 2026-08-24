package worldline.smoke.weathermatrixb173;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.src.WorldInfo;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Captures one rain/thunder countdown transition and its NBT round-trip. */
public final class WeatherStateMatrixBackend implements GameBackend {
  private static final boolean[][] FLAGS = {
      {false, false}, {true, false}, {false, false}, {true, true}, {false, false}};
  private static final int[][] TIMES = {{2, 100}, {2, 100}, {100, 2}, {100, 2}, {1, 1}};
  private final long seed;
  private final int weatherCase;
  private final List<int[]> rows = new ArrayList<>();
  private WeatherWorld world;

  WeatherStateMatrixBackend(long seed, int weatherCase) {
    this.seed = seed;
    this.weatherCase = weatherCase;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String name = source.path().getFileName().toString();
    WorldInfo info = new WorldInfo(seed, name);
    info.setIsRaining(FLAGS[weatherCase][0]);
    info.setIsThundering(FLAGS[weatherCase][1]);
    info.setRainTime(TIMES[weatherCase][0]);
    info.setThunderTime(TIMES[weatherCase][1]);
    world = new WeatherWorld(new MemorySaveHandler(info), name, seed);
    world.rand.setSeed(seed + weatherCase);
  }

  @Override
  public void tick() {
    rows.add(world.weather());
    for (int step = 0; step < 3; step++) {
      world.advanceWeather();
      rows.add(world.weather());
    }
    verify();
    int[] current = rows.get(3), persisted = world.persisted();
    for (int index = 0; index < 4; index++)
      require(current[index] == persisted[index], "weather persistence drifted");
    rows.add(persisted);
  }

  void record(CanonicalTrace trace) {
    for (int step = 0; step < 4; step++)
      trace.record("case" + weatherCase + "-step" + step, 0L, 0, rows.get(step));
    trace.record("case" + weatherCase + "-persisted", 0L, 0, rows.get(4));
  }

  @Override
  public void close() {
    world = null;
  }

  private void verify() {
    int[] step1 = rows.get(1), step2 = rows.get(2), step3 = rows.get(3);
    if (weatherCase == 0)
      require(step2[0] == 1 && between(step3[1], 12000, 23999), "dry-to-rain drifted");
    else if (weatherCase == 1)
      require(step2[0] == 0 && between(step3[1], 12000, 179999), "rain-to-dry drifted");
    else if (weatherCase == 2)
      require(step2[2] == 1 && between(step3[3], 3600, 15599), "calm-to-thunder drifted");
    else if (weatherCase == 3)
      require(step2[2] == 0 && between(step3[3], 12000, 179999), "thunder-to-calm drifted");
    else
      require(step1[0] == 1 && step1[2] == 1 && between(step2[1], 12000, 23999)
          && between(step2[3], 3600, 15599), "combined storm drifted");
  }

  private static boolean between(int value, int minimum, int maximum) {
    return value >= minimum && value <= maximum;
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
