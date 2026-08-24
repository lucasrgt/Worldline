import java.io.File;
import java.util.List;

/** Official-name in-memory persistence boundary for lightning. */
@SuppressWarnings("rawtypes")
final class OracleLightningMemorySaveHandler implements om, fl {
  private final ct info;
  private final OracleLightningMemoryChunkLoader chunks = new OracleLightningMemoryChunkLoader();
  OracleLightningMemorySaveHandler(long seed, String name) {
    info = new ct(seed, name);
    info.a(8, 64, 8);
  }
  public ct c() { return info; }
  public void b() { }
  public an a(os provider) { return chunks; }
  public void a(ct worldInfo, List players) { }
  public void a(ct worldInfo) { }
  public fl d() { return this; }
  public void e() { }
  public File b(String name) { return null; }
  public void a(em player) { }
  public void b(em player) { }
}
