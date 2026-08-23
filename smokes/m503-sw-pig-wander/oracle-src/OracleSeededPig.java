/** Official-name pig exposing only deterministic inherited behavior seeding. */
final class OracleSeededPig extends oc {
  OracleSeededPig(dj world) {
    super(world);
  }
  void seedBehavior(long seed) {
    bv.setSeed(seed);
  }
}
