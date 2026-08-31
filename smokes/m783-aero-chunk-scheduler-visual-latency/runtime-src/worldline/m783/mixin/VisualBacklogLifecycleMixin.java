package worldline.m783.mixin;

import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m783.VisualProbe;
import worldline.m783.VisualRendererStats;

@Mixin(WorldRenderer.class)
public abstract class VisualBacklogLifecycleMixin implements VisualRendererStats {
    @Shadow private List dirtyChunks;
    @Shadow private ChunkBuilder[] chunks;

    @Inject(method = "setWorld(Lnet/minecraft/world/World;)V", at = @At("HEAD"))
    private void worldlineReset(World world, CallbackInfo callback) {
        VisualProbe.worldReset();
    }

    @Override public int worldlineCompileBacklog() { return dirtyChunks.size(); }
    @Override public ChunkBuilder[] worldlineChunks() { return chunks; }
}
