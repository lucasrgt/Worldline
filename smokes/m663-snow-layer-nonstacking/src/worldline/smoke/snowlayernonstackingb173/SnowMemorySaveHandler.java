package worldline.smoke.snowlayernonstackingb173;

import java.io.File;
import java.util.List;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.IPlayerFileData;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WorldProvider;

/** Keeps one explicitly wet or dry WorldInfo while discarding persistent I/O. */
@SuppressWarnings("rawtypes")
final class SnowMemorySaveHandler implements ISaveHandler, IPlayerFileData {
    private final WorldInfo info;

    SnowMemorySaveHandler(long seed, String name, boolean snowfall) {
        info = new WorldInfo(seed, name);
        info.setIsRaining(snowfall);
        info.setRainTime(1000);
    }

    @Override
    public WorldInfo func_22096_c() {
        return info;
    }

    @Override
    public void func_22091_b() {
    }

    @Override
    public IChunkLoader func_22092_a(WorldProvider provider) {
        return new SnowMemoryChunkLoader();
    }

    @Override
    public void func_22095_a(WorldInfo worldInfo, List players) {
    }

    @Override
    public void func_22094_a(WorldInfo worldInfo) {
    }

    @Override
    public IPlayerFileData func_22090_d() {
        return this;
    }

    @Override
    public void func_22093_e() {
    }

    @Override
    public File func_28111_b(String name) {
        return null;
    }

    @Override
    public void writePlayerData(net.minecraft.src.EntityPlayer player) {
    }

    @Override
    public void readPlayerData(net.minecraft.src.EntityPlayer player) {
    }
}
