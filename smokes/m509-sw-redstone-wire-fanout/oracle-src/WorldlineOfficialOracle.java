import worldline.trace.CanonicalTrace;
/** Official-JAR counterpart of the torch-fed T fan-out. */
public final class WorldlineOfficialOracle {
  private static final long SEED = 50920240820L;
  private static final int TX = 8, SX = 9, JX = 10, Y = 65, Z = 8;
  private WorldlineOfficialOracle() {
  }
  public static void main(String[] a) {
    System.setProperty("java.awt.headless", "true");
    dj w =
        new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke"), "worldline-smoke", SEED, null);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        w.c(x, z);
    req(w.b(TX, Y, Z, na.aR.bn, 5), "torch placement failed");
    wire(w, SX, Z);
    wire(w, JX, Z);
    wire(w, JX, Z - 1);
    wire(w, JX, Z + 1);
    CanonicalTrace t = new CanonicalTrace(SEED);
    w.h();
    snap(t, "powered-t", w);
    req(w.e(JX, Y, Z + 1, 0), "branch removal failed");
    w.h();
    snap(t, "south-disconnected", w);
    req(w.e(TX, Y, Z, 0), "source removal failed");
    w.h();
    snap(t, "source-removed", w);
    req(w.c(SX, Y, Z) == 0 && w.c(JX, Y, Z) == 0 && w.c(JX, Y, Z - 1) == 0
            && w.a(JX, Y, Z + 1) == 0,
        "fan-out did not depower");
    t.emitTo(System.out);
  }
  private static void wire(dj w, int x, int z) {
    req(w.e(x, Y, z, na.aw.bn), "wire placement failed");
  }
  private static void snap(CanonicalTrace t, String l, dj w) {
    t.record(l, w.m(), w.b.size(), w.a(TX, Y, Z), w.c(SX, Y, Z), w.c(JX, Y, Z), w.a(JX, Y, Z - 1),
        w.c(JX, Y, Z - 1), w.a(JX, Y, Z + 1), w.c(JX, Y, Z + 1));
  }
  private static void req(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
