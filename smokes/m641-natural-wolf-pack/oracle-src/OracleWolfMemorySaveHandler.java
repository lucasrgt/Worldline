import java.io.File;
import java.util.List;

/** Official-name in-memory world with the global spawn outside the active matrix. */
@SuppressWarnings("rawtypes")
final class OracleWolfMemorySaveHandler implements om, fl {
  private final ct info;
  OracleWolfMemorySaveHandler(long seed, String name) {
    info = new ct(seed, name);
    info.a(30000000, 64, 30000000);
  }
  public ct c() {
    return info;
  }
  public void b() {}
  public an a(os provider) {
    return new OracleWolfMemoryChunkLoader();
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
