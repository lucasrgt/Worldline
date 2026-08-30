package worldline.m773.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m773.SchedulerProbe;

@Mixin(value = ChunkBuilder.class, priority = 900)
public abstract class SchedulerChunkBuilderMixin {
    @Unique private long worldlineRebuildStarted;

    @Inject(method = "rebuild()V", at = @At("HEAD"))
    private void worldlineRebuildBegin(CallbackInfo callback) {
        worldlineRebuildStarted = SchedulerProbe.beginRebuild();
    }

    @Inject(method = "rebuild()V", at = @At("RETURN"))
    private void worldlineRebuildEnd(CallbackInfo callback) {
        SchedulerProbe.endRebuild((ChunkBuilder) (Object) this, worldlineRebuildStarted);
        worldlineRebuildStarted = 0L;
    }
}
