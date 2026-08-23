import worldline.trace.CanonicalTrace;
/** Executes the clicked-ore random-tick fixture against the official obfuscated server JAR. */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class WorldlineOfficialOracle {
  private static final long SEED = 51120240820L;
  private static final int X = 8, Y = 65, Z = 8, NX = 9;
  private WorldlineOfficialOracle() {
  }
  public static void main(String[] a) {
    System.setProperty("java.awt.headless", "true");
    OracleDeterministicWorld w = new OracleDeterministicWorld(
        new OracleMemorySaveHandler(SEED, "worldline-smoke"), "worldline-smoke", SEED);
    for (int x = -9; x <= 9; x++)
      for (int z = -9; z <= 9; z++)
        w.c(x, z);
    em p = new em(w) {};
    p.c(8.5D, 65D, 8.5D, 0F, 0F);
    w.d.add(p);
    w.freezeRandom(SEED);
    require(w.e(X, Y, Z, na.aO.bn) && w.e(NX, Y, Z, na.aO.bn), "ore fixture placement failed");
    CanonicalTrace t = new CanonicalTrace(SEED);
    snap(t, "seeded", w, 0);
    na.aO.b(w, X, Y, Z, p);
    require(w.a(X, Y, Z) == na.aP.bn, "ore did not activate");
    snap(t, "triggered", w, 0);
    int tick = 0;
    while (tick < 2000 && w.a(X, Y, Z) != na.aO.bn) {
      w.h();
      tick++;
    }
    require(w.a(X, Y, Z) == na.aO.bn, "ore did not revert within 2000 random ticks");
    snap(t, "random-reverted", w, tick);
    require(w.a(NX, Y, Z) == na.aO.bn, "untouched ore drifted");
    t.emitTo(System.out);
  }
  private static void snap(CanonicalTrace t, String l, dj w, int tick) {
    t.record(l, w.m(), w.b.size(), tick, w.a(X, Y, Z), w.c(X, Y, Z), w.a(NX, Y, Z), w.c(NX, Y, Z));
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
