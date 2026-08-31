package worldline.m783;

import net.minecraft.client.render.chunk.ChunkBuilder;

/** Narrow read-only boundary over the official client's chunk scheduler. */
public interface VisualRendererStats {
    int worldlineCompileBacklog();
    ChunkBuilder[] worldlineChunks();
}
