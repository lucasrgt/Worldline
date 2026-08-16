package worldline.b173;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.World;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WorldProvider;

/** Deterministic in-memory persistence boundary for the b1.7.3 client. */
@SuppressWarnings("rawtypes")
final class B173MemoryWorld implements ISaveHandler, IChunkLoader {
    private static final int HEIGHT = 128;
    private static final int STONE_TOP = 64;
    private final WorldInfo info;
    private final B173VirtualFileSystem files;

    B173MemoryWorld(long seed, String name, B173VirtualFileSystem files) {
        info = new WorldInfo(seed, name);
        info.setSpawn(8, 65, 8);
        this.files = files;
    }

    public WorldInfo loadWorldInfo() { files.record("world.loadInfo"); return info; }
    public void func_22150_b() { files.record("world.lock"); }
    public IChunkLoader getChunkLoader(WorldProvider provider) { return this; }
    public void saveWorldInfoAndPlayer(WorldInfo worldInfo, List players) {
        files.record("world.savePlayers");
    }
    public void saveWorldInfo(WorldInfo worldInfo) { files.record("world.saveInfo"); }
    public File func_28113_a(String name) { files.record("world.file"); return null; }

    public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
        files.record("chunk.load");
        byte[] blocks = new byte[16 * HEIGHT * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= STONE_TOP; y++) {
                    blocks[x << 11 | z << 7 | y] =
                            (byte) (y == 0 ? Block.bedrock.blockID : Block.stone.blockID);
                }
            }
        }
        Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
        chunk.isTerrainPopulated = true;
        chunk.neverSave = true;
        chunk.func_1024_c();
        return chunk;
    }

    public void saveChunk(World world, Chunk chunk) throws IOException { files.record("chunk.save"); }
    public void saveExtraChunkData(World world, Chunk chunk) throws IOException {
        files.record("chunk.saveExtra");
    }
    public void func_814_a() { files.record("chunk.flush"); }
    public void saveExtraData() { files.record("chunk.saveAll"); }
}
