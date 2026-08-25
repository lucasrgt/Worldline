import java.io.File;
import java.util.List;

/** Official-name wet or dry WorldInfo with in-memory chunks. */
@SuppressWarnings("rawtypes")
final class OracleSnowMemorySaveHandler implements om, fl {
  private final ct info;
  OracleSnowMemorySaveHandler(long seed, String name, boolean snowfall) {
    info = new ct(seed, name);
    info.b(snowfall);
    info.c(1000);
  }
  public ct c() {
    return info;
  }
  public void b() {}
  public an a(os provider) {
    return new OracleSnowMemoryChunkLoader();
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
