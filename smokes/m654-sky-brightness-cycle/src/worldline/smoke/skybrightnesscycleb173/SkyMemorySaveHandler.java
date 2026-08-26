package worldline.smoke.skybrightnesscycleb173;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.minecraft.src.Chunk;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.IPlayerFileData;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.World;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WorldProvider;

/** In-memory save boundary for a clear world that never loads terrain. */
@SuppressWarnings("rawtypes")
final class SkyMemorySaveHandler implements ISaveHandler, IPlayerFileData {
    private final WorldInfo info;
    private final IChunkLoader chunks = new EmptyChunks();

    SkyMemorySaveHandler(long seed, String name) {
        info = new WorldInfo(seed, name);
        info.setSpawnPosition(0, 64, 0);
    }

    public WorldInfo func_22096_c() {
        return info;
    }

    public void func_22091_b() { }

    public IChunkLoader func_22092_a(WorldProvider provider) {
        return chunks;
    }

    public void func_22095_a(WorldInfo worldInfo, List players) { }

    public void func_22094_a(WorldInfo worldInfo) { }

    public IPlayerFileData func_22090_d() {
        return this;
    }

    public void func_22093_e() { }

    public File func_28111_b(String name) {
        return null;
    }

    public void writePlayerData(EntityPlayer player) { }

    public void readPlayerData(EntityPlayer player) { }

    private static final class EmptyChunks implements IChunkLoader {
        public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
            throw new IOException("sky brightness fixture does not load chunks");
        }

        public void saveChunk(World world, Chunk chunk) throws IOException { }

        public void saveExtraChunkData(World world, Chunk chunk) throws IOException { }

        public void func_661_a() { }

        public void saveExtraData() { }
    }
}
