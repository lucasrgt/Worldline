package worldline.smoke.naturalwolfpackb173;

import java.io.File;
import java.util.List;
import net.minecraft.src.*;

/** In-memory world metadata with the global spawn kept outside the active matrix. */
@SuppressWarnings("rawtypes")
final class WolfMemorySaveHandler implements ISaveHandler, IPlayerFileData {
  private final WorldInfo info;
  WolfMemorySaveHandler(long seed, String name) {
    info = new WorldInfo(seed, name);
    info.setSpawnPosition(30000000, 64, 30000000);
  }
  public WorldInfo func_22096_c() {
    return info;
  }
  public void func_22091_b() {}
  public IChunkLoader func_22092_a(WorldProvider provider) {
    return new WolfMemoryChunkLoader();
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
