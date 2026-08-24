import java.io.File;
import java.util.List;

/** Official-name mutable WorldInfo boundary for the weather fixture. */
@SuppressWarnings("rawtypes")
final class OracleMemorySaveHandler implements om, fl {
  private final ct info;
  OracleMemorySaveHandler(ct info) { this.info = info; }
  public ct c() { return info; }
  public void b() { }
  public an a(os provider) { return new OracleEmptyChunkLoader(); }
  public void a(ct worldInfo, List players) { }
  public void a(ct worldInfo) { }
  public fl d() { return this; }
  public void e() { }
  public File b(String name) { return null; }
  public void a(em player) { }
  public void b(em player) { }
}
