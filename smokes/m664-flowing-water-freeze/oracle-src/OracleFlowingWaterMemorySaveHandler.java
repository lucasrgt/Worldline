import java.io.File;
import java.util.List;

/** Official-name in-memory save boundary for the freeze fixture. */
@SuppressWarnings("rawtypes")
final class OracleFlowingWaterMemorySaveHandler implements om, fl {
  private final ct info;

  OracleFlowingWaterMemorySaveHandler(long seed, String name) {
    info = new ct(seed, name);
  }

  public ct c() {
    return info;
  }

  public void b() {
  }

  public an a(os provider) {
    return new OracleFlowingWaterMemoryChunkLoader();
  }

  public void a(ct worldInfo, List players) {
  }

  public void a(ct worldInfo) {
  }

  public fl d() {
    return this;
  }

  public void e() {
  }

  public File b(String name) {
    return null;
  }

  public void a(em player) {
  }

  public void b(em player) {
  }
}
