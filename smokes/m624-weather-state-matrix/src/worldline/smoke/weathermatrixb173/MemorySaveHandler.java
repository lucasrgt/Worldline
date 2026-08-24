package worldline.smoke.weathermatrixb173;

import java.io.File;
import java.util.List;
import net.minecraft.src.*;

/** Keeps one mutable WorldInfo while discarding persistent I/O. */
@SuppressWarnings("rawtypes")
final class MemorySaveHandler implements ISaveHandler, IPlayerFileData {
  private final WorldInfo info;

  MemorySaveHandler(WorldInfo info) { this.info = info; }
  public WorldInfo func_22096_c() { return info; }
  public void func_22091_b() { }
  public IChunkLoader func_22092_a(WorldProvider provider) { return new EmptyChunkLoader(); }
  public void func_22095_a(WorldInfo worldInfo, List players) { }
  public void func_22094_a(WorldInfo worldInfo) { }
  public IPlayerFileData func_22090_d() { return this; }
  public void func_22093_e() { }
  public File func_28111_b(String name) { return null; }
  public void writePlayerData(EntityPlayer player) { }
  public void readPlayerData(EntityPlayer player) { }
}
