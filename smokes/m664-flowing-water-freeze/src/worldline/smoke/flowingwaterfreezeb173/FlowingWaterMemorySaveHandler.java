package worldline.smoke.flowingwaterfreezeb173;

import java.io.File;
import java.util.List;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.IPlayerFileData;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WorldProvider;

/** In-memory save boundary for the mapped freeze fixture. */
@SuppressWarnings("rawtypes")
final class FlowingWaterMemorySaveHandler implements ISaveHandler, IPlayerFileData {
  private final WorldInfo info;

  FlowingWaterMemorySaveHandler(long seed, String name) {
    info = new WorldInfo(seed, name);
  }

  public WorldInfo func_22096_c() {
    return info;
  }

  public void func_22091_b() {
  }

  public IChunkLoader func_22092_a(WorldProvider provider) {
    return new FlowingWaterMemoryChunkLoader();
  }

  public void func_22095_a(WorldInfo worldInfo, List players) {
  }

  public void func_22094_a(WorldInfo worldInfo) {
  }

  public IPlayerFileData func_22090_d() {
    return this;
  }

  public void func_22093_e() {
  }

  public File func_28111_b(String name) {
    return null;
  }

  public void writePlayerData(EntityPlayer player) {
  }

  public void readPlayerData(EntityPlayer player) {
  }
}
