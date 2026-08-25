import java.util.ArrayList;
import java.util.List;

/** Official-name fresh world for one native peaceful-spawn attempt. */
final class OracleWolfPackWorld extends dj {
  OracleWolfPackWorld(om handler, String name, long seed) {
    super(handler, name, seed, null);
  }
  @SuppressWarnings("unchecked")
  void prepare() {
    int centerX = 0, centerZ = 0;
    boolean found = false;
    for (int cx = -128; cx <= 128 && !found; cx++)
      for (int cz = -128; cz <= 128; cz++) {
        gs biome = a().a(cx * 16 + 8, cz * 16 + 8);
        if (biome == gs.d || biome == gs.g) {
          centerX = cx;
          centerZ = cz;
          found = true;
          break;
        }
      }
    if (!found)
      throw new IllegalStateException("seed has no wolf biome in search boundary");
    for (int cx = centerX - 8; cx <= centerX + 8; cx++)
      for (int cz = centerZ - 8; cz <= centerZ + 8; cz++) c(cx, cz);
    d.add(new OracleWolfPlayer(this, centerX * 16, centerZ * 16));
  }
  void spawn(long randomSeed) {
    r.setSeed(randomSeed);
    bp.a(this, false, true);
  }
  @SuppressWarnings("rawtypes")
  int coherentPackSize() {
    List<eh> wolves = new ArrayList<>();
    for (Object entity : b)
      if (entity instanceof eh)
        wolves.add((eh) entity);
    int best = 0;
    for (eh anchor : wolves) {
      int nearby = 0;
      for (eh wolf : wolves) {
        double dx = anchor.aP - wolf.aP, dz = anchor.aR - wolf.aR;
        if (dx * dx + dz * dz <= 24D * 24D)
          nearby++;
      }
      best = Math.max(best, Math.min(nearby, 8));
    }
    return best;
  }
}
