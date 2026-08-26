package worldline.smoke.poweredcreeperb173;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.IPlayerFileData;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.World;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WorldProvider;

/** Minimal in-memory world persistence for the powered-creeper fixture. */
@SuppressWarnings("rawtypes")
final class PoweredCreeperMemorySave implements ISaveHandler, IPlayerFileData {
    private final WorldInfo info;
    private final Chunks chunks = new Chunks();

    PoweredCreeperMemorySave(long seed, String name) {
        info = new WorldInfo(seed, name);
        info.setSpawnPosition(8, 64, 8);
    }

    public WorldInfo func_22096_c() { return info; }
    public void func_22091_b() { }
    public IChunkLoader func_22092_a(WorldProvider provider) { return chunks; }
    public void func_22095_a(WorldInfo worldInfo, List players) { }
    public void func_22094_a(WorldInfo worldInfo) { }
    public IPlayerFileData func_22090_d() { return this; }
    public void func_22093_e() { }
    public File func_28111_b(String name) { return null; }
    public void writePlayerData(EntityPlayer player) { }
    public void readPlayerData(EntityPlayer player) { }

    private static final class Chunks implements IChunkLoader {
        private static final int HEIGHT = 128;

        @Override public Chunk loadChunk(World world, int chunkX, int chunkZ) {
            byte[] blocks = new byte[16 * HEIGHT * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y <= 64; y++) {
                        set(blocks, x, y, z,
                                y == 0 ? Block.bedrock.blockID : Block.stone.blockID);
                    }
                }
            }
            Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
            chunk.isTerrainPopulated = true;
            chunk.neverSave = true;
            chunk.func_353_b();
            return chunk;
        }

        private static void set(byte[] blocks, int x, int y, int z, int id) {
            blocks[x << 11 | z << 7 | y] = (byte) id;
        }

        @Override public void saveChunk(World world, Chunk chunk) throws IOException { }
        @Override public void saveExtraChunkData(World world, Chunk chunk) throws IOException { }
        @Override public void func_661_a() { }
        @Override public void saveExtraData() { }
    }
}
