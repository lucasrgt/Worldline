/** Fails closed if the weather-only oracle unexpectedly requests terrain. */
final class OracleEmptyChunkLoader implements an {
  public hi a(dj world, int chunkX, int chunkZ) {
    throw new IllegalStateException("weather fixture requested a chunk");
  }
  public void a(dj world, hi chunk) { }
  public void b(dj world, hi chunk) { }
  public void a() { }
  public void b() { }
}
