package worldline.m773;

import net.minecraft.client.render.chunk.ChunkBuilder;

/** Narrow read-only boundary over WorldRenderer's chunk scheduler state. */
public interface SchedulerRendererStats {
    int worldlineCompileBacklog();
    ChunkBuilder[] worldlineChunks();
}
