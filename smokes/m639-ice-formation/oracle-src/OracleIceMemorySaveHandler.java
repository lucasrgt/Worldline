import java.io.File;
import java.util.List;

/** Official-name in-memory save boundary for ice formation. */
@SuppressWarnings("rawtypes")
final class OracleIceMemorySaveHandler implements om, fl {
  private final ct info;
  private final boolean lit;
  OracleIceMemorySaveHandler(long seed, String name, boolean lit) {
    info = new ct(seed, name);
    this.lit = lit;
  }
  public ct c() {
    return info;
  }
  public void b() {}
  public an a(os provider) {
    return new OracleIceMemoryChunkLoader(lit);
  }
  public void a(ct worldInfo, List players) {}
  public void a(ct worldInfo) {}
  public fl d() {
    return this;
  }
  public void e() {}
  public File b(String name) {
    return null;
  }
  public void a(em player) {}
  public void b(em player) {}
}
