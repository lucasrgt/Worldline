package worldline.m775.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m775.MatrixProbe;

@Mixin(value = ChunkBuilder.class, priority = 900)
public abstract class MatrixChunkBuilderMixin {
    @Unique private long worldlineRebuildStarted;

    @Inject(method = "rebuild()V", at = @At("HEAD"))
    private void worldlineRebuildBegin(CallbackInfo callback) {
        worldlineRebuildStarted = MatrixProbe.beginRebuild();
    }

    @Inject(method = "rebuild()V", at = @At("RETURN"))
    private void worldlineRebuildEnd(CallbackInfo callback) {
        MatrixProbe.endRebuild(worldlineRebuildStarted);
        worldlineRebuildStarted = 0L;
    }
}
