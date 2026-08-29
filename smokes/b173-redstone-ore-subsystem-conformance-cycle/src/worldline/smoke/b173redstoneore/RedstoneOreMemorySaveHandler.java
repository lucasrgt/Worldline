package worldline.smoke.b173redstoneore;

import java.io.File;
import java.util.List;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.IPlayerFileData;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WorldProvider;

/** In-memory world metadata paired with deterministic superflat chunks. */
@SuppressWarnings("rawtypes")
final class RedstoneOreMemorySaveHandler implements ISaveHandler, IPlayerFileData {
    private final WorldInfo info;
    private final RedstoneOreMemoryChunkLoader chunks = new RedstoneOreMemoryChunkLoader();

    RedstoneOreMemorySaveHandler(long seed, String name) {
        info = new WorldInfo(seed, name);
        info.setSpawnPosition(8, 64, 8);
    }

    public WorldInfo func_22096_c() {
        return info;
    }
    public void func_22091_b() {
    }
    public IChunkLoader func_22092_a(WorldProvider provider) {
        return chunks;
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
