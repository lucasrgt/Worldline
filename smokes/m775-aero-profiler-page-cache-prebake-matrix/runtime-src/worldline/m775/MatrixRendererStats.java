package worldline.m775;

import net.minecraft.client.render.chunk.ChunkBuilder;

/** Narrow read-only boundary over the official client's chunk scheduler. */
public interface MatrixRendererStats {
    int worldlineCompileBacklog();
    ChunkBuilder[] worldlineChunks();
}
