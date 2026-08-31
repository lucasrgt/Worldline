package worldline.m783.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m783.VisualProbe;

@Mixin(value = ChunkBuilder.class, priority = 900)
public abstract class VisualChunkBuilderMixin {
    @Shadow public int x;
    @Shadow public int z;
    @Unique private long worldlineRebuildStarted;

    @Inject(method = "rebuild()V", at = @At("HEAD"))
    private void worldlineRebuildBegin(CallbackInfo callback) {
        worldlineRebuildStarted = VisualProbe.beginRebuild();
    }

    @Inject(method = "rebuild()V", at = @At("RETURN"))
    private void worldlineRebuildEnd(CallbackInfo callback) {
        VisualProbe.endRebuild(worldlineRebuildStarted, x, z);
        worldlineRebuildStarted = 0L;
    }
}
