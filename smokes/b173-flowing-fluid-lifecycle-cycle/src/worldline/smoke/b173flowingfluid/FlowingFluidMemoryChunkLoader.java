package worldline.smoke.b173flowingfluid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.World;

/** In-memory chunk persistence for a real native save and chunk-reload boundary. */
final class FlowingFluidMemoryChunkLoader implements IChunkLoader {
    private static final int HEIGHT = 128;
    private final Map<Long, Snapshot> saved = new HashMap<Long, Snapshot>();

    @Override public Chunk loadChunk(World world, int chunkX, int chunkZ) throws IOException {
        Snapshot snapshot = saved.get(key(chunkX, chunkZ));
        byte[] blocks = snapshot == null ? terrain() : snapshot.blocks.clone();
        Chunk chunk = new Chunk(world, blocks, chunkX, chunkZ);
        if (snapshot != null) {
            restoreMetadata(chunk, snapshot.metadata);
        }
        chunk.isTerrainPopulated = true;
        chunk.neverSave = false;
        chunk.func_353_b();
        return chunk;
    }

    @Override public void saveChunk(World world, Chunk chunk) throws IOException {
        byte[] blocks = new byte[16 * HEIGHT * 16];
        byte[] metadata = new byte[blocks.length];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < HEIGHT; y++) {
                    int index = index(x, y, z);
                    blocks[index] = (byte) chunk.getBlockID(x, y, z);
                    metadata[index] = (byte) chunk.getBlockMetadata(x, y, z);
                }
            }
        }
        saved.put(key(chunk.xPosition, chunk.zPosition), new Snapshot(blocks, metadata));
    }

    @Override public void saveExtraChunkData(World world, Chunk chunk) throws IOException {
    }

    @Override public void func_661_a() {
    }

    @Override public void saveExtraData() {
    }

    private static byte[] terrain() {
        byte[] blocks = new byte[16 * HEIGHT * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= 64; y++) {
                    blocks[index(x, y, z)] = (byte) (y == 0
                            ? Block.bedrock.blockID : Block.stone.blockID);
                }
            }
        }
        return blocks;
    }

    private static void restoreMetadata(Chunk chunk, byte[] metadata) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < HEIGHT; y++) {
                    int value = metadata[index(x, y, z)] & 15;
                    if (value != 0) {
                        chunk.setBlockIDWithMetadata(
                                x, y, z, chunk.getBlockID(x, y, z), value);
                    }
                }
            }
        }
    }

    private static int index(int x, int y, int z) {
        return x << 11 | z << 7 | y;
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static final class Snapshot {
        final byte[] blocks, metadata;
        Snapshot(byte[] blocks, byte[] metadata) {
            this.blocks = blocks;
            this.metadata = metadata;
        }
    }
}
