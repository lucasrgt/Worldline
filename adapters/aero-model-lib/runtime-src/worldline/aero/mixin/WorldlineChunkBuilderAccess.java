package worldline.aero.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only M15 access to vanilla's built readiness bit. */
@Mixin(ChunkBuilder.class)
public interface WorldlineChunkBuilderAccess {
    @Accessor("built") boolean worldlineBuilt();
}
