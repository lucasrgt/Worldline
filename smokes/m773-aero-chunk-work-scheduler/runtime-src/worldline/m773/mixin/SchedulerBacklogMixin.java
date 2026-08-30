package worldline.m773.mixin;

import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import worldline.m773.SchedulerRendererStats;

@Mixin(WorldRenderer.class)
public abstract class SchedulerBacklogMixin implements SchedulerRendererStats {
    @Shadow private List dirtyChunks;
    @Shadow private ChunkBuilder[] chunks;

    @Override public int worldlineCompileBacklog() { return dirtyChunks.size(); }
    @Override public ChunkBuilder[] worldlineChunks() { return chunks; }
}
