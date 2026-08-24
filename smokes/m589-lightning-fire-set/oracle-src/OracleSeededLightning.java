/** Official-name lightning with stable post-constructor RNG. */
final class OracleSeededLightning extends c {
  OracleSeededLightning(dj world, double x, double y, double z, long seed) {
    super(world, x, y, z);
    bv.setSeed(seed);
  }
}
