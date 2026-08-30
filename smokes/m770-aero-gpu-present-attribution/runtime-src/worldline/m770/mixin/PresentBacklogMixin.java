package worldline.m770.mixin;

import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import worldline.m770.PresentRendererStats;

/** Exposes only the size of vanilla's terrain compile backlog. */
@Mixin(WorldRenderer.class)
public abstract class PresentBacklogMixin implements PresentRendererStats {
    @Shadow private List dirtyChunks;

    @Override public int worldlineCompileBacklog() {
        return dirtyChunks.size();
    }
}
