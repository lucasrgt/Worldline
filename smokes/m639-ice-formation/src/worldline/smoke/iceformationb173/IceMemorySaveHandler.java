package worldline.smoke.iceformationb173;

import java.io.File;
import java.util.List;
import net.minecraft.src.*;

/** Keeps one WorldInfo and an in-memory cold-weather surface. */
@SuppressWarnings("rawtypes")
final class IceMemorySaveHandler implements ISaveHandler, IPlayerFileData {
  private final WorldInfo info;
  private final boolean lit;
  IceMemorySaveHandler(long seed, String name, boolean lit) {
    info = new WorldInfo(seed, name);
    this.lit = lit;
  }
  public WorldInfo func_22096_c() {
    return info;
  }
  public void func_22091_b() {}
  public IChunkLoader func_22092_a(WorldProvider provider) {
    return new IceMemoryChunkLoader(lit);
  }
  public void func_22095_a(WorldInfo worldInfo, List players) {}
  public void func_22094_a(WorldInfo worldInfo) {}
  public IPlayerFileData func_22090_d() {
    return this;
  }
  public void func_22093_e() {}
  public File func_28111_b(String name) {
    return null;
  }
  public void writePlayerData(EntityPlayer player) {}
  public void readPlayerData(EntityPlayer player) {}
}
