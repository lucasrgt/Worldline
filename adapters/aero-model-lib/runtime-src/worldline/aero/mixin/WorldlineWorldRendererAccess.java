package worldline.aero.mixin;

import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only M15 access to vanilla's chunk arrays and dirty queue. */
@Mixin(WorldRenderer.class)
public interface WorldlineWorldRendererAccess {
    @Accessor("dirtyChunks") List<ChunkBuilder> worldlineDirtyChunks();
    @Accessor("chunks") ChunkBuilder[] worldlineChunks();
}
