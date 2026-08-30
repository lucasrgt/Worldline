package worldline.m775.mixin;

import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import worldline.m775.MatrixRendererStats;

@Mixin(WorldRenderer.class)
public abstract class MatrixBacklogMixin implements MatrixRendererStats {
    @Shadow private List dirtyChunks;
    @Shadow private ChunkBuilder[] chunks;

    @Override public int worldlineCompileBacklog() { return dirtyChunks.size(); }
    @Override public ChunkBuilder[] worldlineChunks() { return chunks; }
}
