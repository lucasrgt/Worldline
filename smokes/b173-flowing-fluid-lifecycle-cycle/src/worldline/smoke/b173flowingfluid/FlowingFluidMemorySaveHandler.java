package worldline.smoke.b173flowingfluid;

import java.io.File;
import java.util.List;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.IPlayerFileData;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WorldProvider;

/** Retains native chunk snapshots while replacing filesystem I/O. */
@SuppressWarnings("rawtypes")
final class FlowingFluidMemorySaveHandler implements ISaveHandler, IPlayerFileData {
    private final WorldInfo info;
    private final FlowingFluidMemoryChunkLoader chunks = new FlowingFluidMemoryChunkLoader();

    FlowingFluidMemorySaveHandler(long seed, String name) {
        info = new WorldInfo(seed, name);
        info.setSpawnPosition(8, 65, 8);
    }

    @Override public WorldInfo func_22096_c() {
        return info;
    }

    @Override public void func_22091_b() {
    }

    @Override public IChunkLoader func_22092_a(WorldProvider provider) {
        return chunks;
    }

    @Override public void func_22095_a(WorldInfo worldInfo, List players) {
    }

    @Override public void func_22094_a(WorldInfo worldInfo) {
    }

    @Override public IPlayerFileData func_22090_d() {
        return this;
    }

    @Override public void func_22093_e() {
    }

    @Override public File func_28111_b(String name) {
        return null;
    }

    @Override public void writePlayerData(EntityPlayer player) {
    }

    @Override public void readPlayerData(EntityPlayer player) {
    }
}
