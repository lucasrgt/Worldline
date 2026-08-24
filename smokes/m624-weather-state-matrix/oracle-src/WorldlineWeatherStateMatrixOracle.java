import java.util.ArrayList;
import java.util.List;
import worldline.trace.CanonicalTrace;

/** Executes five weather transitions directly against the official server JAR. */
public final class WorldlineWeatherStateMatrixOracle {
  private static final long SEED = 62420260823L;
  private static final boolean[][] FLAGS = {
      {false, false}, {true, false}, {false, false}, {true, true}, {false, false}};
  private static final int[][] TIMES = {{2, 100}, {2, 100}, {100, 2}, {100, 2}, {1, 1}};

  private WorldlineWeatherStateMatrixOracle() { }

  public static void main(String[] arguments) {
    CanonicalTrace trace = new CanonicalTrace(SEED);
    for (int weatherCase = 0; weatherCase < 5; weatherCase++)
      run(trace, weatherCase);
    trace.emitTo(System.out);
  }

  private static void run(CanonicalTrace trace, int weatherCase) {
    ct info = new ct(SEED, "weather-" + weatherCase);
    info.b(FLAGS[weatherCase][0]);
    info.a(FLAGS[weatherCase][1]);
    info.c(TIMES[weatherCase][0]);
    info.b(TIMES[weatherCase][1]);
    OracleWeatherWorld world = new OracleWeatherWorld(new OracleMemorySaveHandler(info),
        "weather-" + weatherCase, SEED);
    world.r.setSeed(SEED + weatherCase);
    List<int[]> rows = new ArrayList<>();
    rows.add(values(info, world));
    for (int step = 0; step < 3; step++) {
      world.advanceWeather();
      rows.add(values(info, world));
    }
    verify(rows, weatherCase);
    ct persisted = new ct(info.a());
    int[] finalRow = rows.get(3), copy = values(persisted, null);
    for (int index = 0; index < 4; index++)
      require(finalRow[index] == copy[index], "weather persistence drifted");
    rows.add(copy);
    for (int step = 0; step < 4; step++)
      trace.record("case" + weatherCase + "-step" + step, 0L, 0, rows.get(step));
    trace.record("case" + weatherCase + "-persisted", 0L, 0, rows.get(4));
  }

  private static int[] values(ct info, OracleWeatherWorld world) {
    return new int[] {info.l() ? 1 : 0, info.m(), info.j() ? 1 : 0, info.k(),
        world == null ? -1 : world.rainStrength(), world == null ? -1 : world.thunderStrength()};
  }

  private static void verify(List<int[]> rows, int weatherCase) {
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
    if (!condition) throw new IllegalStateException(message);
  }
}
