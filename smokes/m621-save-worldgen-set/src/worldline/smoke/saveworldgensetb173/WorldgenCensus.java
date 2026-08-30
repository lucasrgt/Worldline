package worldline.smoke.saveworldgensetb173;

import worldline.api.RemoteWorldView;
import worldline.testkit.TerrainGenerationFixture;

/** Smoke-local compatibility wrapper over the public terrain generation fixture. */
final class WorldgenCensus {
  private final TerrainGenerationFixture.Evidence evidence;

  private WorldgenCensus(TerrainGenerationFixture.Evidence evidence) {
    this.evidence = evidence;
  }

  static WorldgenCensus measure(RemoteWorldView world, int minX, int maxX, int minZ, int maxZ) {
    return new WorldgenCensus(TerrainGenerationFixture.observe(world, minX, maxX, minZ, maxZ));
  }

  int surfaceFamilies() { return evidence.surfaceFamilies(); }
  int caveAir() { return evidence.caveAir(); }
  int oreBlocks() { return evidence.oreBlocks(); }
  int oreVeins() { return evidence.oreComponents(); }

  boolean replayEquals(WorldgenCensus value) {
    return value != null && evidence.replayEquals(value.evidence);
  }

  String describe() { return evidence.describe(); }

  @Override public boolean equals(Object other) {
    return other instanceof WorldgenCensus
        && evidence.equals(((WorldgenCensus) other).evidence);
  }

  @Override public int hashCode() { return evidence.hashCode(); }
}
